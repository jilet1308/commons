package com.jilet.commons.shared.exception;

public interface JiletExceptions {

    // 1-50: Cryptography-related errors
    JiletException ENCRYPTION_ERROR = new JiletException(1, 500, "Encryption failed");
    JiletException ENCRYPTION_KEY_NULL = new JiletException(2, 500, "Encryption key must not be null");
    JiletException ENCRYPTION_INPUT_NULL = new JiletException(3, 500, "Input to encrypt must not be null");
    JiletException DECRYPTION_ERROR = new JiletException(4, 500, "Decryption failed");
    JiletException DECRYPTION_KEY_NULL = new JiletException(5, 500, "Decryption key must not be null");
    JiletException DECRYPTION_INPUT_NULL = new JiletException(6, 500, "Input to decrypt must not be null");
    JiletException DECRYPTING_NON_ENCRYPTED_VALUE = new JiletException(7, 500, "Attempting to decrypt a value that does not appear to be encrypted");
}
