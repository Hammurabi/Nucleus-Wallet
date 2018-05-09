package com.riverssen.core.security;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

public class Wallet
{
    public static       KeyFactory Factory = null;
    public static final Wallet     DEFAULT = new Wallet("DefaultWallet", "DefaultWallet");

    static {
        try{
            Factory = KeyFactory.getInstance("ECDSA", "BC");
        } catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private PrivKey privateKey;
    private PubKey  publicKey;
    private String  name;

    public Wallet(String name, String seed)
    {
        this(name, seed.getBytes());
    }

    public Wallet()
    {
    }

    public Wallet(String name, byte   seed[])
    {
        this.name = name;
        try
        {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(seed);
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();

            this.privateKey = new PrivKey(keyPair.getPrivate());
            this.publicKey  = new PubKey(keyPair.getPublic());
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Wallet(PubKey key)
    {
        this.publicKey = key;
    }

    public PrivKey getPrivateKey()
    {
        return privateKey;
    }

    public void setPrivateKey(PrivKey privateKey)
    {
        this.privateKey = privateKey;
    }

    public PubKey getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(PubKey publicKey)
    {
        this.publicKey = publicKey;
    }

    public int export(String password, File parentDir)
    {
        try
        {
            File diry = new File(parentDir + File.separator + name + File.separator);
            diry.mkdirs();
            File file = new File(parentDir + File.separator + name + File.separator + name + ".rwt");
            File pub = new File(parentDir + File.separator + name + File.separator + "readme.txt");
            File publik = new File(parentDir + File.separator + name + File.separator + "public.txt");

            FileOutputStream writer     = new FileOutputStream(file);
            DataOutputStream stream     = new DataOutputStream(writer);
            FileWriter       writer2    = new FileWriter(publik);

            writer2.write(getPublicKey().getPublicWalletAddress());

            writer2.flush();
            writer2.close();

            byte out[] = privateKey.getPrivate().getEncoded();

            if (password != null)
            {
                AdvancedEncryptionStandard aes = new AdvancedEncryptionStandard(password.getBytes());
                out = aes.encrypt(out);
            }

            stream.writeShort(publicKey.getPublicAddress().getBytes().length);
            stream.writeShort(out.length);

            stream.write(publicKey.getPublicAddress().getBytes());
            stream.write(out);

            stream.flush();
            stream.close();

            final String api = "https://api.qrserver.com/v1/create-qr-code/?size=512x512&data=" + publicKey.getPublicWalletAddress();

            FileWriter writer1 = new FileWriter(pub);

            writer1.write("This file and the image are public, so don't worry about sharing it with people! But keep the rwt secure and don't forget your password! \n\n");
            writer1.write(publicKey.getPublicWalletAddress());
            writer1.write("\n\n\t<3 Riverssen\n");

            writer1.flush();
            writer1.close();

            try
            {
                BufferedImage image = ImageIO.read(new URL(api));

                ImageIO.write(image, "PNG", new File(parentDir + File.separator + name + File.separator + "img.png"));
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        } catch (Exception e)
        {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }
}