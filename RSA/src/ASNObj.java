


public abstract class ASNObj{
	public abstract Encoder getEncoder();
	public byte[] encode(){
		//System.out.println("will encode: " +this);
		return getEncoder().getBytes();
	}
	public abstract Object decode(Decoder dec) throws ASN1DecoderFail;
	public ASNObj instance() throws CloneNotSupportedException{return (ASNObj) clone();}
}
