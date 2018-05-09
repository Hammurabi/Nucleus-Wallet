package com.riverssen.core.security;

import com.riverssen.utils.HashUtil;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.PrivateKey;
import java.security.Signature;

public class PrivKey
{
    private PrivateKey key;

    public PrivKey(PrivateKey key)
    {
        this.key = key;
    }

    public boolean verifySignature(PubKey pky, byte data[])
    {
        return false;
    }

    public Key getPrivate()
    {
        return key;
    }
}
