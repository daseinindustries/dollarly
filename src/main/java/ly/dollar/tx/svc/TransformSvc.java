package ly.dollar.tx.svc;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;

public class TransformSvc {
	private final PooledPBEStringEncryptor pbe;

	public TransformSvc() {
		this.pbe = new PooledPBEStringEncryptor();
		pbe.setAlgorithm("PBEWithMD5AndTripleDES");
		pbe.setPassword("bumper8wel105dk75s0999k");
		pbe.setPoolSize(4);
	}

	public String transformMessage(String message) {
		if (message == null) {
			return null;
		} else {
			if(message.length() == 4){
				return message;
			} else {
			return this.pbe.decrypt(message);
			}
		}
	}
}
