require 'openssl'
require 'ostruct'
require 'tmpdir'
require 'securerandom'

require 'puppetserver/ca/action/generate'
require 'puppetserver/ca/action/setup'
require 'puppetserver/ca/host'
require 'puppetserver/ca/logger'
require 'puppetserver/ca/utils/file_system'

module PuppetserverSpec
  module Ca
    class Pki
      include Puppetserver::Ca::Utils
      attr :ca_cert, :ca_crl, :ca_key, :root_cert, :root_crl, :root_key, :settings

      SETTINGS_TEMPLATE = {
        confdir: '%<confdir>s',
        ssldir: '%<confdir>s/ssl',
        cadir: '%<confdir>s/ca',
        certdir: '%<confdir>s/ssl/certs',
        privatekeydir: '%<confdir>s/ssl/private_keys',
        publickeydir: '%<confdir>s/ssl/public_keys',
        hostpubkey: '%<confdir>s/ssl/public_keys/localhost.pem',
        hostprivkey: '%<confdir>s/ssl/private_keys/localhost.pem',
        hostcert: '%<confdir>s/ssl/certs/localhost.pem',
        hostcrl: '%<confdir>s/ssl/crl.pem',
        localcacert: '%<confdir>s/ssl/certs/ca.pem',
        csrdir: '%<confdir>s/ca/requests',
        signeddir: '%<confdir>s/ca/signed',
        cakey: '%<confdir>s/ca/ca_key.pem',
        capub: '%<confdir>s/ca/ca_pub.pem',
        cacert: '%<confdir>s/ca/ca_crt.pem',
        cacrl: '%<confdir>s/ca/ca_crl.pem',
        serial: '%<confdir>s/ca/serial',
        rootkey: '%<confdir>s/ca/root_key.pem',
        cert_inventory: '%<confdir>s/ca/inventory.txt',
        keylength: 2048,
        certname: 'localhost',
        ca_name: 'Puppet CA: localhost',
        root_ca_name: 'Puppet Root CA: %<root_ca_name_rand>s',
        ca_ttl: 157_680_000,
        subject_alt_names: '',
        csr_attributes: ''
      }.freeze

      def initialize(settings = {}, intermediate_cert: true)
        settings_vars = {
          confdir: (Dir.mktmpdir 'puppetca-', ENV['TMPDIR'] || '/tmp'),
          root_ca_name_rand: SecureRandom.hex(7)
        }
        @settings = SETTINGS_TEMPLATE.merge(settings).transform_values { |v| (v.is_a? String) ? v % settings_vars : v }

        signer = SigningDigest.new
        @digest = signer.digest
        @logger = Puppetserver::Ca::Logger.new(:warning, $stdout, $stderr)
        @ca = Puppetserver::Ca::LocalCertificateAuthority.new(@digest, @settings)

        @root_key, @root_cert, @root_crl = @ca.create_root_cert

        if intermediate_cert
          @ca.create_intermediate_cert(@root_key, @root_cert)
          ca_cert_setting = [@ca.cert, @root_cert]
          ca_crl_setting = [@ca.crl, @root_crl]
        else
          ca_ssl = OpenStruct.new(
            {
              cert: root_cert,
              certs: root_cert,
              crl: root_crl,
              crls: root_crl,
              key: root_key
            }
          )
          ca_cert_setting = @root_cert
          ca_crl_setting = @root_crl
          @ca.load_ssl_components(ca_ssl)
        end

        @ca_crl = @ca.crl
        @ca_cert = @ca.cert
        @ca_key = @ca.key

        FileSystem.ensure_dirs([@settings[:ssldir],
                                @settings[:cadir],
                                @settings[:certdir],
                                @settings[:privatekeydir],
                                @settings[:publickeydir],
                                @settings[:signeddir]])

        @ca.update_serial_file(2)
        server_key, server_cert = @ca.create_server_cert
        inventory = @ca.inventory_entry(@ca.cert) + "\n" + @ca.inventory_entry(server_cert)

        [
          [@settings[:cacert], ca_cert_setting],
          [@settings[:cacrl], ca_crl_setting],
          [@settings[:cadir] + '/infra_crl.pem', ca_crl_setting],
          [@settings[:hostcert], server_cert],
          [@settings[:localcacert], ca_cert_setting],
          [@settings[:hostcrl], ca_crl_setting],
          [@settings[:hostpubkey], server_key.public_key],
          [@settings[:capub], @ca.key.public_key],
          [@settings[:cert_inventory], inventory],
          [@settings[:cadir] + '/infra_inventory.txt', ''],
          [@settings[:cadir] + '/infra_serials', ''],
          [File.join(@settings[:signeddir], "#{@settings[:certname]}.pem"), server_cert],
          [@settings[:hostprivkey], server_key],
          [@settings[:rootkey], @root_key],
          [@settings[:cakey], @ca.key]
        ].each do |location, content|
          FileSystem.write_file(location, content, 0644)
        end

        @ca.update_serial_file(server_cert.serial + 1)
      end

      def generate_cert(certnames, alt_names = [])
        certnames = certnames.is_a?(Array) ? certnames : [certnames]

        generate = Puppetserver::Ca::Action::Generate.new(@logger)
        generate.generate_authorized_certs(certnames, alt_names, @settings, @digest)

        certnames.each do |certname|
          add_inventory_cert("#{@settings[:signeddir]}/#{certname}.pem")
        end
      end

      def add_inventory_cert(certfile)
        cert = OpenSSL::X509::Certificate.new(File.read(certfile))
        File.open(@settings[:cert_inventory], 'a') do |f|
          f.puts(@ca.inventory_entry(cert))
        end
      end

      def generate_key_csr(certname, alt_names = [])
        generate = Puppetserver::Ca::Action::Generate.new(@logger)
        _key, csr = generate.generate_key_csr(certname, @settings, @digest, alt_names)

        Dir.mkdir @settings[:csrdir] unless Dir.exist? @settings[:csrdir]
        generate.save_file(csr, certname, @settings[:csrdir], 'Certificate request')
      end

      def revoke_cert(certname)
        crl = OpenSSL::X509::CRL.new(File.read(@settings[:cacrl]))
        cert = OpenSSL::X509::Certificate.new(File.read("#{@settings[:certdir]}/#{certname}.pem"))

        revocation = OpenSSL::X509::Revoked.new
        revocation.serial = cert.serial
        revocation.time = Time.now
        crl.add_revoked(revocation)

        ca_key = OpenSSL::PKey::RSA.new(File.read(@settings[:cakey]))
        crl.sign(ca_key, @digest)

        File.write(@settings[:cacrl], crl.to_pem)
      end

      def create_intermediate_cert(certname, ca_key = @root_key, ca_cert = @root_cert)
        host = Puppetserver::Ca::Host.new(@digest)

        key = host.create_private_key(@settings[:keylength])
        int_csr = host.create_csr(name: certname, key: key)
        cert = @ca.sign_intermediate(ca_key, ca_cert, int_csr)

        [cert, key]
      end

      def create_crl(cert: @ca.cert, key: @ca.key, serial: 0, deltaserial: nil, akid: true, last_update: just_now, next_update: valid_until)
        crl = OpenSSL::X509::CRL.new
        crl.issuer = cert.subject
        crl.version = 1

        ef = @ca.extension_factory_for(cert)
        crl.add_extension(ef.create_extension(['authorityKeyIdentifier', 'keyid:always', false])) if akid
        crl.add_extension(OpenSSL::X509::Extension.new('crlNumber', OpenSSL::ASN1::Integer(serial)))
        crl.add_extension(OpenSSL::X509::Extension.new('2.5.29.27', OpenSSL::ASN1::Integer(deltaserial), true)) \
          unless deltaserial.nil?

        crl.last_update = last_update
        crl.next_update = next_update
        crl.sign(key, @digest)

        crl
      end

      def create_root_crl(serial, deltaserial = nil, akid: true)
        create_crl(cert: @root_cert, key: @root_key, serial: serial, deltaserial: deltaserial, akid: akid)
      end

      def just_now
        Time.now - 1
      end

      def valid_until
        Time.now + @settings[:ca_ttl]
      end
    end
  end
end

def regen_crl_tests_pki
  puts 'Regenerating PKI for puppetlabs.ssl-utils.core-test/validate-cert-chain-and-crls-test ...'

  settings = {
    'root_ca_name': 'Root CA',
    'ca_name': 'Intermediate CA 1'
  }

  pki = PuppetserverSpec::Ca::Pki.new(settings)
  ica2_cert, ica2_key = pki.create_intermediate_cert('Intermediate CA 2', pki.ca_key, pki.ca_cert)
  ica3_cert, ica3_key = pki.create_intermediate_cert('Intermediate CA 3', pki.ca_key, pki.ca_cert)
  root_crl = pki.create_root_crl(0)
  ica2_crl = pki.create_crl(cert: ica2_cert, key: ica2_key)
  ica3_crl = pki.create_crl(cert: ica3_cert, key: ica3_key)
  unrelated_pki = PuppetserverSpec::Ca::Pki.new(settings)

  dest_dir = "#{__dir__}/puppetlabs/ssl_utils/examples/ssl/crl_validation"
  File.open("#{dest_dir}/1-cert-chain.pem", 'w') do |f|
    f.puts(pki.root_cert)
  end
  File.open("#{dest_dir}/1-crl-chain.pem", 'w') do |f|
    f.puts(pki.create_root_crl(0))
  end
  File.open("#{dest_dir}/2-crl-chain.pem", 'w') do |f|
    f.puts(pki.ca_crl, ica2_crl)
  end
  File.open("#{dest_dir}/3-cert-chain.pem", 'w') do |f|
    f.puts(ica2_cert, pki.ca_cert, pki.root_cert)
  end
  File.open("#{dest_dir}/3-crl-chain.pem", 'w') do |f|
    f.puts(root_crl, pki.ca_crl, ica2_crl)
  end
  File.open("#{dest_dir}/4-crl-chain.pem", 'w') do |f|
    f.puts(root_crl, pki.ca_crl, ica3_crl, ica2_crl)
  end
  File.open("#{dest_dir}/cert-with-expired-crl.pem", 'w') do |f|
    f.puts(pki.root_cert)
  end
  File.open("#{dest_dir}/expired-crl.pem", 'w') do |f|
    f.puts(pki.create_crl(cert: pki.root_cert,
                          key: pki.root_key,
                          last_update: Time.now - pki.settings[:ca_ttl] * 2,
                          next_update: Time.now - pki.settings[:ca_ttl]))
  end
  File.open("#{dest_dir}/cert-with-crl-bad-sig.pem", 'w') do |f|
    f.puts(pki.root_cert)
  end
  File.open("#{dest_dir}/crl-with-bad-signature.pem", 'w') do |f|
    f.puts(pki.create_crl(cert: unrelated_pki.root_cert,
                          key: unrelated_pki.root_key))
  end
  File.open("#{dest_dir}/cert-with-not-valid-crl.pem", 'w') do |f|
    f.puts(pki.root_cert)
  end
  File.open("#{dest_dir}/not-yet-valid-crl.pem", 'w') do |f|
    f.puts(pki.create_crl(cert: pki.root_cert,
                          key: pki.root_key,
                          last_update: Time.now + 86_400))
  end
  File.open("#{dest_dir}/cert-chain-with-revoked-cert.pem", 'w') do |f|
    f.puts(ica2_cert, pki.ca_cert, pki.root_cert)
  end
  File.open("#{dest_dir}/crl-chain-with-cert-revoked.pem", 'w') do |f|
    revocation = OpenSSL::X509::Revoked.new
    revocation.serial = ica2_cert.serial
    revocation.time = Time.now
    root_crl.add_revoked(revocation)
    signer = Puppetserver::Ca::Utils::SigningDigest.new
    root_crl.sign(pki.root_key, signer.digest)
    f.puts(root_crl, pki.ca_crl, ica2_crl)
  end
  File.open("#{dest_dir}/other-3-cert-chain.pem", 'w') do |f|
    f.puts(ica2_cert, pki.ca_cert, pki.root_cert)
  end
  FileUtils.cp unrelated_pki.settings[:cacrl], "#{dest_dir}/other-3-crl-chain.pem"
end

regen_crl_tests_pki
