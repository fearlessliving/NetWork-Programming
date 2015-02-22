/*License: GNU General Public License version 2*/


public abstract class ASNObj {
	public abstract Encoder getEncoder();
	public byte[] encode(){
		return getEncoder().getBytes();
	}
	public abstract Object decode(Decoder dec) throws ASN1DecoderFail;
	public ASNObj instance() throws CloneNotSupportedException{return (ASNObj) clone();}
}
