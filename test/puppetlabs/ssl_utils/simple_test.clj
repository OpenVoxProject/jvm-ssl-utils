(ns puppetlabs.ssl-utils.simple-test
  (:require [clojure.test :refer :all]
            [puppetlabs.ssl-utils.simple :as simple]
            [puppetlabs.ssl-utils.core :as ssl-utils]
            [puppetlabs.ssl-utils.testutils :as testutils])
  (:import (java.io ByteArrayOutputStream ByteArrayInputStream)))

(defn roundtrip-pem
  [to-pem-fn from-pem-fn obj]
  (let [outstream (ByteArrayOutputStream.)]
    (to-pem-fn obj outstream)
    (let [instream (ByteArrayInputStream. (.toByteArray outstream))]
      (from-pem-fn instream))))

(deftest basic-ca-cert-crl-test
  (testing "Can generate a valid CA cert, cert, and CRL through simple API"
    (let [ca-cert (simple/gen-self-signed-cert "ca" 1 {} true)
          cert (simple/gen-cert "foo.localdomain" ca-cert 2)
          crl (simple/gen-crl ca-cert)
          read-ca-cert (roundtrip-pem ssl-utils/cert->pem! ssl-utils/pem->cert (:cert ca-cert))
          read-cert (roundtrip-pem ssl-utils/cert->pem! ssl-utils/pem->cert (:cert cert))
          read-crl (roundtrip-pem ssl-utils/crl->pem! ssl-utils/pem->crl crl)]
      (is (ssl-utils/certificate? read-ca-cert))
      (is (ssl-utils/certificate? read-cert))
      (is (ssl-utils/certificate-revocation-list? read-crl))
      (is (= "ca" (ssl-utils/get-cn-from-x509-certificate read-ca-cert)))
      (is (= "foo.localdomain" (ssl-utils/get-cn-from-x509-certificate read-cert)))
      (is (= "ca" (ssl-utils/get-cn-from-x500-principal (.getIssuerX500Principal read-cert))))
      (is (= "ca" (ssl-utils/get-cn-from-x500-principal (.getIssuerX500Principal read-crl)))))))

(deftest long-puppet-common-name-test
  (testing "certnames longer than the RFC 5280 CN bound of 64 characters work through the simple API"
    ;; Each generation step sits inside its own `is` so that when it throws,
    ;; it reports a single error and the remaining steps still run, instead
    ;; of the exception aborting the whole deftest.
    (let [long-certname testutils/long-puppet-cn
          ca-cert (is (simple/gen-self-signed-cert long-certname 1 {:keylength 512} true))
          cert (when ca-cert
                 (is (simple/gen-cert long-certname ca-cert 2 {:keylength 512})))
          crl (when ca-cert
                (is (simple/gen-crl ca-cert)))]
      (when ca-cert
        (is (simple/ssl-cert? ca-cert))
        (is (= long-certname (ssl-utils/get-cn-from-x509-certificate (:cert ca-cert)))))
      (when cert
        (is (simple/ssl-cert? cert))
        (is (= long-certname (ssl-utils/get-cn-from-x509-certificate (:cert cert)))))
      (when crl
        (is (ssl-utils/certificate-revocation-list? crl))))))

(deftest optional-parameters-test
  (testing "Can specify keylength when generating a certificate"
    (let [cacert (simple/gen-self-signed-cert "CA" 0)
          cert (simple/gen-cert "foo" cacert 1 {:keylength 512})]
      (is (= simple/default-keylength (ssl-utils/keylength (:public-key cacert))))
      (is (= simple/default-keylength (ssl-utils/keylength (:private-key cacert))))
      (is (= 512 (ssl-utils/keylength (:public-key cert))))
      (is (= 512 (ssl-utils/keylength (:private-key cert))))))
  (testing "Can specify extensions when generating a certificate"
    (let [extensions [(ssl-utils/subject-dns-alt-names ["bar" "baz"] false)]
          cacert (simple/gen-self-signed-cert "CA" 0)
          cert (simple/gen-cert "foo" cacert 1 {:extensions extensions})]
      (is (= [] (ssl-utils/get-extensions (:cert cacert))))
      (is (= ["bar" "baz"] (ssl-utils/get-subject-dns-alt-names (:cert cert)))))))
