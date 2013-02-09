package ly.dollar.tx.svc;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class TransformSvcImpl {

	private StandardPBEStringEncryptor ste;
	private static final String pass = "blargh8bbbblarg";

	public TransformSvcImpl() {
		ste = new StandardPBEStringEncryptor();
		ste.setAlgorithm("PBEWithMD5AndTripleDES");
		ste.setPassword(pass);
	}

	public String transform(String val) {
		String digest = ste.encrypt(val);
		return digest;
	}

	public String resolve(String digest) {
		String ingest = ste.decrypt(digest);
		return ingest;
	}
	
}
