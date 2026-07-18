package com.puppetlabs.ssl_utils;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.X500NameStyle;
import org.bouncycastle.asn1.x500.style.BCStyle;

/**
 * BCStyle subclass that does not enforce the RFC 5280 upper bound
 *
 * BouncyCastle 1.85 started enforcing the 64 character limit on
 * Common Name fields. This presents a problem for Puppet certificate
 * usage as the CN is usually populated with the Fully Qualified
 * Domain Name of a node, which DNS allows to be up to 253 characters.
 *
 * Some usages can push past that, for example the default CA subject
 * of:
 *
 *   Puppet CA: $fqdn
 *
 * For now, this class implements the relaxed behavior of BouncyCastle
 * 1.84. It may gain a 253 character limit in the near future, and
 * may disappear entirely in the far future if a method to preserve
 * `certname` semantics is found while also complying with the
 * RFC 5280 limit.
 *
 * @see https://github.com/bcgit/bc-java/commit/a6ae24643e7ba7cfdf9267656ce83bb656ab9777
 */
public class PuppetCNStyle extends BCStyle {
  public static final X500NameStyle INSTANCE = new PuppetCNStyle();

  protected PuppetCNStyle() { }

  @Override
  protected ASN1Encodable encodeStringValue(ASN1ObjectIdentifier oid, String value) {
    // When using the FIPS libraries, ASN1ObjectIdentifier.equals()
    // is not implemented. So, cast to string and compare against
    // the OID value for Common Name (CN).
    if (oid.toString().equals("2.5.4.3")) {
      // TODO: throw IllegalArgumentException if CN is longer than 253
      //       characters. Breaking change. Requires a major version bump.

      // encode directly, bypassing BCStyle's 64-char cap; a CN is a
      // DirectoryString, and BC's default DirectoryString encoding is
      // a UTF8String (as done by AbstractX500NameStyle for all other
      // string-valued attributes).
      return new DERUTF8String(value);
    }

    // every other attribute keeps the standard BCStyle behaviour,
    // including the country-code and other DirectoryString bounds.
    return super.encodeStringValue(oid, value);
  }
}
