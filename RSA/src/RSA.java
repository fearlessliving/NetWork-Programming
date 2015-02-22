import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

public class RSA {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub


		
		Generater generater =new Generater();
		
		if(args.length==0)
		{
			System.out.println("input is not valid,please input again!");
		}
		else if(args.length==1&&args[0].equalsIgnoreCase("-h"))
		{
			System.out.println("-----help information-----");
			System.out.println("this program will take a while to execute,please be paitent!");
		    System.out.println("plaintext_file should be the txt file,but when you execute");
		    System.out.println("please do not add the extention name txt,just put the file name directly.");
		    System.out.println("for example: java RSA -e -p plaintextfile -k publickey -c ciphertextfile");
		    System.out.println("please do not add extention name txt");
		    System.out.println("please execute this program by this command as belowed:");
		    System.out.println("java RSA -K -P public_key_file -S secret_key_file -r Miller_Rabin_rounds");
		    System.out.println("java RSA -e -p plaintext_file -k public_key_file -c ciphertext_file");
		    System.out.println("java RSA -d -c ciphertext_file -k secret_key_file -p plaintext_file");
		    
		    
		    
		    
		    
		    
		    
			
			
		}
		else if(args.length==7&&args[0].equalsIgnoreCase("-K"))
		{
			generater.generateParameters();
			//generater.ASN1Encode("publickey", "privatekey"); 
			generater.ASN1Encode(args[2], args[5]); 
			
			
			
		}
		else if(args.length==7&&args[0].equalsIgnoreCase("-e"))
		{
			generater.encrypt(args[2],args[6],args[4]);
			//-e -p plaintextfile.txt -k publickey -c ciphertextfile.txt
			//generater.encrypt("plaintextfile.txt","ciphertextfile.txt","publickey");
			System.out.println("encryption is finished!");
			
			
		}
		else if(args.length==7&&args[0].equalsIgnoreCase("-d"))
		{
			generater.decrypt(args[2],args[6],args[4]);
			//-d -c ciphertextfile.txt -k privatekey -p plainresult.txt
			//generater.decrypt("ciphertextfile.txt","plainresult.txt","privatekey");
			System.out.println("decryption is finished!");
			
		}
		
		
		/*
		generater.generateParameters();
		generater.ASN1Encode("publickey", "privatekey"); 
		//generater.ASN1Decode("publickey");
		//generater.ASN1Decode("privatekey");
		ciphertext=generater.encrypt("plaintextfile.txt","ciphertextfile.txt","publickey");
		generater.decrypt("ciphertextfile.txt","plainresult.txt","privatekey");
               
         */      
          
	}

}


class Generater {
	
	     //modulus: n, publicExponent: e, privateExponent: d, prime1: p, prime2: q, exponent1: d mod (p-1), exponent2: d mod (q-1), coefficient: q-1 mod p)
		 public BigInteger e;
		 public BigInteger n;	
		 public BigInteger phi;
		 public BigInteger d;
		 public BigInteger p;
		 public BigInteger q;
	     public BigInteger dP;
	     public BigInteger dQ;
	     public BigInteger coefficient;
	     
	   

	     
	     public void generateParameters()
	     {
	    	 
	    	  System.out.println("Generating All the parameter is going to take a while! Please be patient!");
	    	  MillerRabinTestPrime test =new MillerRabinTestPrime();
	    	  
	    	  p = test.genPrime(1024);
	      
	          q = test.genPrime(1024);
	       
	          phi=(p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
	       
	          
	          //public key e,n
	          n=p.multiply(q); 
	          e=new BigInteger("65537");
	       
	          //private key d
	          d=e.modInverse(phi);
	          dP=d.mod(p.subtract(BigInteger.ONE));
			  dQ=d.mod(q.subtract(BigInteger.ONE));
			  coefficient=q.modInverse(p);
			
			  System.out.println("Generating All the parameter is finished!");
	    	     	 
	     }
	     
	     
	
	     public void ASN1Encode(String pubKeyFile, String priKeyFile) {
		  
			FileOutputStream pubfout = null;
			FileOutputStream prifout = null;

			try {

				// open output file
				try {
					pubfout = new FileOutputStream(pubKeyFile);
					prifout = new FileOutputStream(priKeyFile);
				} catch (FileNotFoundException exc) {
					System.err.println("Error Opening Output File");
				}
			} catch (ArrayIndexOutOfBoundsException exc) {
				System.err.println("Usage: Write Key to File");
			}

			Encoder modulus = new Encoder(n);
			Encoder publicExponent = new Encoder(e);
			Encoder pubKeySeq = (new Encoder()).initSequence()
					.addToSequence(modulus).addToSequence(publicExponent);

			Encoder version = new Encoder(BigInteger.ZERO);
			Encoder privateExponent = new Encoder(d);
			Encoder prime1 = new Encoder(p);
			Encoder prime2 = new Encoder(q);
			Encoder exponent1 = new Encoder(dP);
			Encoder exponent2 = new Encoder(dQ);
			Encoder qInvfficient = new Encoder(coefficient);
			Encoder priKeySeq = (new Encoder()).initSequence()
					.addToSequence(version).addToSequence(modulus)
					.addToSequence(publicExponent).addToSequence(privateExponent)
					.addToSequence(prime1).addToSequence(prime2)
					.addToSequence(exponent1).addToSequence(exponent2)
					.addToSequence(qInvfficient);
			try {
				pubfout.write(pubKeySeq.getBytes());
				pubfout.flush();
				prifout.write(priKeySeq.getBytes());
				prifout.flush();
				pubfout.close();
				prifout.close();
			} catch (IOException exc) {
				System.err.println("File Write Error.");
			}

			
		}

		
		
		public  void ASN1Decode(String keyFile) {

			FileInputStream fin = null;
			byte test[] = new byte[2000];
			int length = 0;

			try {
				fin = new FileInputStream(keyFile);
			} catch (FileNotFoundException exc) {
				System.out.println("Input File Not Found");
				return;
			}
			try {

				length = fin.read(test);

			} catch (IOException exc) {
				System.err.println("File Read Error.");
			}

			byte pubKey[] = new byte[length];
			for (int i = 0; i < length; i++)
				pubKey[i] = test[i];

			Decoder decoder = new Decoder(pubKey, 0);
			decoder = decoder.getContent();
			if (length < 1000) {
		
				n=decoder.getFirstObject(true).getInteger();
				System.out.println("modulus: "+ n);
			    e=decoder.getFirstObject(true).getInteger();
				System.out.println("exponent: "+ e);
			} else {

				System.out.println("version: "
						+ decoder.getFirstObject(true).getInteger());

				n= decoder.getFirstObject(true).getInteger();
				System.out.println("modulus: "
						+n);
				e= decoder.getFirstObject(true).getInteger();
				System.out.println("publicExponent: "
						+e);

				d=decoder.getFirstObject(true).getInteger();
				System.out.println("privateExponent: "
						+ d);
				p=decoder.getFirstObject(true).getInteger();
				System.out.println("prime1: "
						+ p);

				q= decoder.getFirstObject(true).getInteger();
				System.out.println("prime2: "
						+q);
				
				dP=decoder.getFirstObject(true).getInteger();
				System.out.println("exponent1: "
						+ dP);

				dQ=decoder.getFirstObject(true).getInteger();
				System.out.println("exponent2: "
						+ dQ);
				
				coefficient=decoder.getFirstObject(true).getInteger();
				System.out.println("coefficient: "
						+ coefficient);

			}
		}
		
		
		
		public String encrypt(String plaintextFileName,String ciphertextFileName,String publickeyFileName) {

			BigInteger message=null;
			BigInteger ciphertext=null;
			FileInputStream fileInputStream = null;
			FileOutputStream pubfout = null;
			byte test[] = new byte[2000];
			int length = 0;

			try {
				//read plaintext from file
				fileInputStream = new FileInputStream(plaintextFileName+".txt");//plaintextfile.txt
				length = fileInputStream.read(test);
				byte[] temp = new byte[length];
				for(int i=0;i<length;i++){
					temp[i]=test[i];	
				}
				//convert plaintext to biginteger
			    message = new BigInteger(temp);
			    
			    
				//RSA encryption
			    ASN1Decode(publickeyFileName);//publickey
			   
			    ciphertext = message.modPow(e, n);
                //write to the file
				pubfout = new FileOutputStream(ciphertextFileName+".txt");//ciphertextfile.txt
				pubfout.write(ciphertext.toByteArray());
				
				
				fileInputStream.close();
				pubfout.flush();
				pubfout.close();

			} 
			catch (FileNotFoundException exc) {
				System.out.println("Input File Not Found");
			}
			catch (IOException exc) {
				System.err.println("File Read Error.");
			}
	
		    return ciphertext.toString();  
		}
		
		
		
		public void decrypt(String ciphertextFileName,String plaintextFileName,String privatekeyFileName) {
		
			FileInputStream fileInputStream = null;
			FileOutputStream pubfout = null;
			byte test[] = new byte[2000];
			int length = 0;

			
			try {
				//read ciphertext from file
			    fileInputStream = new FileInputStream(ciphertextFileName+".txt");//ciphertextfile.txt
				length = fileInputStream.read(test);
				byte[] temp = new byte[length];
				for(int i=0;i<length;i++){
					temp[i]=test[i];
					
				}
				//RSA decryption
				ASN1Decode(privatekeyFileName);//privatekey
				
				BigInteger beTest = new BigInteger(temp);
				BigInteger m1 = beTest.modPow(dP, p);
				BigInteger m2 = beTest.modPow(dQ, q);
				BigInteger h = m1.subtract(m2).multiply(coefficient).mod(p);
				BigInteger m = m2.add(q.multiply(h));
				//write to the file
				pubfout = new FileOutputStream(plaintextFileName+".txt");//plainresult.txt
				pubfout.write(m.toByteArray());
				
				
			} catch (FileNotFoundException exc) {
				System.out.println("Input File Not Found");
			}
		     catch (IOException exc) {
				System.err.println("File Read Error.");
			}
		}
		
		
		
		
		
}

class MillerRabinTestPrime {

    //Creates secure random numbers
    private static final Random rnd = new Random();

    private static boolean miller_rabin_pass(BigInteger a, BigInteger n) {
        BigInteger n_minus_one = n.subtract(BigInteger.ONE);
        BigInteger d = n_minus_one;
        int s = d.getLowestSetBit();
        d = d.shiftRight(s);
        BigInteger a_to_power = a.modPow(d, n);
        if (a_to_power.equals(BigInteger.ONE))
            return true;
        for (int i = 0; i < s - 1; i++) {
            if (a_to_power.equals(n_minus_one))
                return true;
            a_to_power = a_to_power.multiply(a_to_power).mod(n);
        }
        if (a_to_power.equals(n_minus_one))
            return true;
        return false;
    }

    public static boolean miller_rabin(BigInteger n) {
        for (int repeat = 0; repeat < 5; repeat++) {
            BigInteger a;
            do {
                a = new BigInteger(n.bitLength(), rnd);
            } while (a.equals(BigInteger.ZERO));
            if (!miller_rabin_pass(a, n)) {
                return false;
            }
        }
        return true;
    }
    
    public BigInteger genPrime(int bitsize) {
        BigInteger p;
        do {
            p = new BigInteger(bitsize, rnd);
            //test for small factors
            if (p.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO))
                continue;
            if (p.mod(BigInteger.valueOf(3)).equals(BigInteger.ZERO))
                continue;
            if (p.mod(BigInteger.valueOf(5)).equals(BigInteger.ZERO))
                continue;
            if (p.mod(BigInteger.valueOf(7)).equals(BigInteger.ZERO))
                continue;
        } while (!miller_rabin(p));
        return p;
    }

   
}