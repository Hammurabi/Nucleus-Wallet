package com.riverssen.core.security;

import com.riverssen.utils.Base58;
import com.riverssen.utils.HashUtil;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;

public class PubKey
{
    public static final int SIZE_IN_BYTES = 85;

    private PublicKey key;
    private String    ads;
    private String    wad;
    private String    publicWalletAddress;

    public PubKey(byte address[])
    {
        this.key = addressToPublicKey(address);
        this.ads = publicKeyToAddress();
    }

    public PubKey(String address)
    {
        this.key = addressToPublicKey(address);
        this.ads = publicKeyToAddress();
    }

    public PubKey(PublicKey key)
    {
        this.key = key;
        this.ads = publicKeyToAddress();
    }

    public byte[] sign(String data)
    {
        return sign(data.getBytes());
    }

    public byte[] sign(byte data[])
    {
        return null;
    }

    public Key getPublic()
    {
        return key;
    }

    private String publicKeyToAddress()
    {
        try{
            ECPublicKey key = (ECPublicKey) this.key;

            ECPoint point = key.getW();

            ByteBuffer pubKeyBuffer = ByteBuffer.allocate(point.getAffineX().toByteArray().length + point.getAffineY().toByteArray().length + 1);

//            System.out.println(point.getAffineX().toByteArray().length + " " + point.getAffineY().toByteArray().length);

//            pubKeyBuffer.put(RVCCore.versionByte);
            pubKeyBuffer.put((byte)point.getAffineX().toByteArray().length);
//            pubKeyBuffer.put((byte)point.getAffineY().toByteArray().length);

            pubKeyBuffer.put(point.getAffineX().toByteArray());
            pubKeyBuffer.put(point.getAffineY().toByteArray());
            pubKeyBuffer.flip();

            byte sha256[]   = HashUtil.applySha256(pubKeyBuffer.array());
            byte sha2562[]  = HashUtil.applySha256(sha256);
            byte ripeMD[]   = HashUtil.applyRipeMD160(sha2562);

            wad = Base58.encode(ripeMD);
//            System.out.println(new BigInteger(pubKeyBuffer.array()).toByteArray().length);
//            System.out.println(new BigInteger(new BigInteger(pubKeyBuffer.array()).toString(36), 36).toByteArray().length);
//            System.out.println("-----------------");
            return HashUtil.base64StringEncode(pubKeyBuffer.array());

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return "0";
    }

    private PublicKey addressToPublicKey(byte key[])
    {
        try{
            int xs = Byte.toUnsignedInt(key[0]);
//            int ys = Byte.toUnsignedInt(key[1]);

//            System.out.println(xs + " " + ys);
//            System.out.println(xs + " " + ((key.length - 2) - ys - 1));

            byte x[] = Arrays.copyOfRange(key, 1, xs + 1);
            byte y[] = Arrays.copyOfRange(key, xs + 1, (key.length - 1 - xs) + xs + 1);

            ECPoint point = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ((ECPublicKey) Wallet.DEFAULT.getPublicKey().getPublic()).getParams());

            return Wallet.Factory.generatePublic(pubSpec);
        } catch (Exception e)
        {
//            Logger.err("couldn't convert publicaddresskey to publickey");
//            e.printStackTrace();
        }

        return null;
    }

    private PublicKey addressToPublicKey(String key)
    {
        String realKey = key.substring(0);
        return addressToPublicKey(HashUtil.base64StringDecode(key));
    }

    public String getPublicAddress()
    {
        return ads;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof PubKey) return key.equals(((PubKey) obj).key);
        return false;
    }

    public boolean isValid()
    {
        return key != null;
    }

    public String toString()
    {
        return this.ads;
    }

    public static byte[] getBytes(String keyAddress)
    {
        return HashUtil.base64StringDecode(keyAddress);
    }

    public String getPublicWalletAddress()
    {
        return wad;
    }

    public byte[] encrypt(String msg)
    {
        byte nMSG[] = new byte[1];

        try {
            Cipher cipher = Cipher.getInstance("ECIES", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return cipher.doFinal(msg.getBytes());
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return nMSG;
    }
}
