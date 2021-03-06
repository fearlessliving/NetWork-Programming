
 


import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Calendar;


public 
class Encoder{
	public static final byte PC_PRIMITIVE=0;
	public static final byte PC_CONSTRUCTED=1;
	public static final byte CLASS_UNIVERSAL=0;
	public static final byte CLASS_APPLICATION=1;
	public static final byte CLASS_CONTEXT=2;
	public static final byte CLASS_PRIVATE=3;
	public static final byte TAG_EOC=0;
	public static final byte TAG_BOOLEAN=1;
	public static final byte TAG_INTEGER=2;
	public static final byte TAG_BIT_STRING=3;
	public static final byte TAG_OCTET_STRING=4;
	public static final byte TAG_NULL=5;
	public static final byte TAG_OID=6;
	public static final byte TAG_ObjectDescriptor=7;
	public static final byte TAG_EXTERNAL=8;
	public static final byte TAG_REAL=9;
	public static final byte TAG_EMBEDDED_PDV=11;
	public static final byte TAG_UTF8String=12;
	public static final byte TAG_RELATIVE_OID=13;
	public static final byte TAG_SEQUENCE=16;
	public static final byte TAG_SET=17;
	public static final byte TAG_NumericString=18;
	public static final byte TAG_PrintableString=19;
	public static final byte TAG_T61String=20;
	public static final byte TAG_VideotextString2=1;
	public static final byte TAG_IA5String=22;
	public static final byte TAG_UTCTime=23;
	public static final byte TAG_GeneralizedTime=24;
	public static final byte TAG_GraphicString=25;
	public static final byte TAG_VisibleString=26;
	public static final byte TAG_GenerlString=27;
	public static final byte TAG_UniversalString=28;
	public static final byte TAG_CHARACTER_STRING=29;
	public static final byte TAG_BMPString=30;
	
	int bytes=0;
	byte[] header_type=new byte[0];
	byte[] header_length=new byte[0];
	Encoder prefix_data=null;
	byte[] data=new byte[0];
	public static void copyBytes(byte[] results, int offset, byte[]src, int length){
		copyBytes(results, offset, src,length,0);
	}
	public static void copyBytes(byte[] results, int offset, byte[]src, int length, int src_offset){
		if(results.length<length+offset)
			System.err.println("Destination too short: "+results.length+" vs "+offset+"+"+length);
		if(src.length<length+src_offset)
			System.err.println("Source too short: "+src.length+" vs "+src_offset+"+"+length);
		
		for(int k=0; k<length; k++) {
			results[k+offset] = src[src_offset+k];
		}
	}
	public static String getGeneralizedTime(long utime){
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(utime);
		return Encoder.getGeneralizedTime(date);
	}
	/*
	public static Calendar getCalendar(String gdate) {
		return Util.getCalendar(gdate);
	}
	*/
	public byte[] getBytes() {
		//System.err.println("Getting Bytes from object size: "+bytes);
		byte[] buffer = new byte[bytes];
		getBytes(buffer, 0);
		return buffer;
	}
	public int getBytes(byte[] results, int start) {
		int offset = start;
		//byte[] results = new byte[bytes];
		copyBytes(results,offset,header_type,header_type.length);
		offset += header_type.length;
		copyBytes(results,offset,header_length,header_length.length);
		offset += header_length.length;
		if(prefix_data!=null){
			offset = prefix_data.getBytes(results, offset);
		}
		copyBytes(results,offset,data,data.length);
		assert(bytes == offset+data.length-start);
		return offset+data.length;
	}
	public Encoder initSequence(){
		header_type=new byte[]{Encoder.TAG_SEQUENCE};
		header_length=new byte[]{0x0};
		bytes=2;
		return this;
	}
	public Encoder initSet(){
		header_type=new byte[]{Encoder.TAG_SET};
		header_length=new byte[]{0x0};
		bytes=2;
		return this;
	}
	static final BigInteger BN127 = new BigInteger(""+127);
	BigInteger contentLength() {
		if(header_length.length==0) return BigInteger.ZERO;
		if(header_length[0]>=0) return new BigInteger(""+header_length[0]);
		byte[] result = new byte[header_length.length-1];
		copyBytes(result,0,header_length,header_length.length-1,1);
		return new BigInteger(result);
	}
	void setASN1Length(BigInteger len){
		//System.err.println("set len="+bytes);
		//this.bytes = bytes;
		int old_len_len = this.header_length.length;
		if(len.compareTo(BN127) <= 0){
			header_length = new byte[]{(byte)bytes};
		}else{
			byte[] len_bytes = len.toByteArray();
			header_length=new byte[1+len_bytes.length];
			header_length[0]=(byte)(len_bytes.length+128);
			copyBytes(header_length,1,len_bytes,len_bytes.length);
		}		
		bytes += header_length.length - old_len_len;		
	}
	void setASN1Length(int bytes){
		int old_len_len = this.header_length.length;
		//System.err.println("set len="+bytes);
		//this.bytes = bytes;
		if(bytes<=127){
			header_length = new byte[]{(byte)bytes};
		}else{
			BigInteger len = new BigInteger(bytes+"");
			byte[] len_bytes = len.toByteArray();
			header_length=new byte[1+len_bytes.length];
			header_length[0]=(byte)(len_bytes.length+128);
			copyBytes(header_length,1,len_bytes,len_bytes.length);
		}		
		//System.err.println("set bytes="+bytes+"+"+header_length.length +"-"+ old_len_len);
		this.bytes += header_length.length - old_len_len;
	}
	void incrementASN1Length(int inc){
		//System.err.println("inc="+inc);
		BigInteger len=contentLength();//new BigInteger(header_length);
		//System.err.println("old_len="+len);
		len=len.add(new BigInteger(""+inc));
		//System.err.println("new_len="+len);
		setASN1Length(len.intValue());
	}
	
	public Encoder setASN1Type(byte tagASN1){
		int old_len_len = this.header_type.length;
		header_type=new byte[]{tagASN1};
		bytes += header_type.length - old_len_len;
		return this;
	}
	/**
	 * 
	 * @param classASN1  The ASN1 Class (UNIVERSAL/APPLICATION/CONTEXT/PRIVATE)
	 * @param PCASN1	Is this PRIMITIVE or CONSTRUCTED
	 * @param tag_number
	 * @return returns this
	 */
	public Encoder setASN1Type(int classASN1, int PCASN1, byte tag_number){
		int tag = (classASN1<<6)+(PCASN1<<5)+tag_number;
		return setASN1Type((byte)tag);
	}
	public Encoder setASN1Type(int classASN1, int PCASN1, BigInteger tag_number){
		int old_len_len = this.header_type.length;
		int tag = (classASN1<<6)+(PCASN1<<5)+0x1f;
		String nb=tag_number.toString(128);
		int tag_len=nb.length();
		header_type=new byte[tag_len+1];
		header_type[0]=(byte)tag;
		for(int k=0; k<tag_len; k++) header_type[k+1]=(byte) (nb.charAt(k)&0x80);
		header_type[tag_len-1] = (byte) nb.charAt(tag_len-1);
		bytes += header_type.length - old_len_len;
		return this;
	}
	protected void setPrefix(Encoder prefix){
		if(prefix_data == null) prefix_data=prefix;
		else {
			prefix_data.setPrefix(prefix);
		}
		bytes += prefix.bytes;
	}
	public Encoder addToSequence(Encoder asn1_data){
		////incrementASN1Length(asn1_data.bytes);
		//bytes += asn1_data.bytes;
		//asn1_data.setPrefix(data);
		//asn1_data.setPrefix(prefix_data);
		//asn1_data.setPrefix(header_len);
		//asn1_data.setPrefix(header_type);
		//prefix_data=asn1_data;
		//System.err.println("addASN1ToASN1Sequence:: "+this+"+"+asn1_data);
		return addToSequence(asn1_data.getBytes());
	}
	public Encoder addToSequence(byte[] asn1_data){
		return addToSequence(asn1_data, 0, asn1_data.length);
	}
	public Encoder addToSequence(byte[] asn1_data, int offset, int length){
		//System.err.println("addASN1ToASN1Sequence_1: "+bytes+"+"+length);
		//System.err.println("addASN1ToASN1Sequence_2: "+this+"+"+Util.byteToHex(asn1_data," "));
		if(data.length==0) {
			data=new byte[length];
			copyBytes(data, 0, asn1_data, length, offset);
			this.incrementASN1Length(length);
			bytes += length;
			//System.err.println("addASN1ToASN1Sequence_r: "+bytes+"+"+length);
		} else {
			Encoder e=new Encoder();
			e.data = data;
			e.prefix_data = prefix_data;
			e.bytes = bytes-header_length.length-header_type.length;
			prefix_data = e;
			data=new byte[0];
			addToSequence(asn1_data, offset, length);
			//System.err.println("addASN1ToASN1Sequence_r2: "+bytes+"+"+length);
		}
		return this;
	}
	public Encoder(){}
	public Encoder(BigInteger b){
		data=b.toByteArray();
		setASN1Type(Encoder.TAG_INTEGER);
		setASN1Length(data.length);
		bytes+=data.length;
		assert(bytes==2+data.length);
	}
	public Encoder(long l){
		data=new BigInteger(""+l).toByteArray();
		setASN1Type(Encoder.TAG_INTEGER);
		setASN1Length(data.length);
		bytes=2+data.length;
	}
	public Encoder(int i){
		data=new BigInteger(""+i).toByteArray();
		setASN1Type(Encoder.TAG_INTEGER);
		setASN1Length(data.length);
		bytes=2+data.length;
	}
	public Encoder(byte b){
		data=new byte[]{b};
		setASN1Type(Encoder.TAG_INTEGER);
		setASN1Length(1);
		bytes=2+data.length;
	}
	public Encoder(boolean b){
		data=new byte[]{b?(byte)1:(byte)0};
		setASN1Type(Encoder.TAG_BOOLEAN);
		setASN1Length(1);
		bytes=2+data.length;
	}
	public static String getGeneralizedTime(Calendar time){
		return String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS.%1$tLZ",time);	
	}
	public Encoder(Calendar time){
		String UTC = getGeneralizedTime(time);
		/*
		String UTC=String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS.%1$tLZ",time);
			time.get(Calendar.YEAR)+time.get(Calendar.MONTH)+
			time.get(Calendar.DAY_OF_MONTH)+time.get(Calendar.HOUR)+
			time.get(Calendar.MINUTE)+time.get(Calendar.SECOND)+"."+
			time.get(Calendar.MILLISECOND));
			*/
		data=UTC.getBytes(Charset.forName("UTF-8"));
		setASN1Type(Encoder.TAG_GeneralizedTime);
		setASN1Length(data.length);
		bytes=2+data.length;		
		assert(bytes==2+data.length);
	}
	public Encoder(int[] oid){
		int len = oid.length-1;
		for(int k=2;k<oid.length; k++){
			if(oid[k]>127) len += new BigInteger(oid[k]+"").toString(128).length()-1;
		}
		data = new byte[len];
		data[0]=(byte)(40*oid[0]+oid[1]);
		int coff = 1;
		for(int k=2;k<oid.length; k++) {
			byte[] b=(new BigInteger(oid[k]+"")).toString(128).getBytes();
			for(int j=0;j<b.length;j++,coff++) data[coff]=(byte)(b[j] | (byte)(128 * ((j<b.length-1)?1:0)));
		}
		setASN1Type(Encoder.TAG_OID);
		setASN1Length(data.length);
		bytes+=data.length;
		assert(bytes==2+data.length);
	}
	public Encoder(double r){
		data = Double.toHexString(r).getBytes();
		setASN1Type(Encoder.TAG_REAL);
		setASN1Length(data.length);
		bytes+=data.length;
	}
	/**
	 * Create a UTF8String Encoder
	 * @param s
	 */
	public Encoder(String s){
		if(s==null){
			this.setNull();
			return;
		}
		data=s.getBytes(Charset.forName("UTF-8"));
		setASN1Type(Encoder.TAG_UTF8String);
		//System.out.println("bytes="+bytes+"+"+data.length);
		setASN1Length(data.length);
		//System.out.println("bytes="+bytes+"+"+data.length);
		bytes+=data.length;
		assert(bytes==2+data.length);
	}
	/**
	 * Create a BIT_STRING padded Encoder
	 * @param s
	 * @param padding_bits
	 */
	public Encoder(byte padding_bits, byte[] s){
		if(s==null){
			this.setNull();
			return;
		}
		data=new byte[s.length+1];
		data[0] = padding_bits;
		Encoder.copyBytes(data, 1, s, s.length);
		setASN1Type(Encoder.TAG_BIT_STRING);
		setASN1Length(data.length);
		bytes+=data.length;
		assert(bytes==2+data.length);
	}
	/**
	 * Create a BIT_STRING like padded Encoder
	 * @param s
	 * @param padding_bits
	 */
	public Encoder( byte padding_bits, byte[] s,byte type){
		if(s==null){
			this.setNull();
			return;
		}
		data=new byte[s.length+1];
		data[0] = padding_bits;
		Encoder.copyBytes(data, 1, s, s.length);
		setASN1Type(type);//Encoder.TAG_BIT_STRING);
		setASN1Length(data.length);
		bytes+=data.length;
		assert(bytes==2+data.length);
	}
	/**
	 * Create an OCTET STRING encoder. 
	 * @param s
	 */
	public Encoder(byte[] s, byte type){
		if(s==null){
			this.setNull();
			return;
		}
		data=s;
		setASN1Type(type);
		setASN1Length(data.length);
		bytes=2+data.length;
		assert(bytes==2+data.length);
	}
	public Encoder(byte[] s){
		if(s==null){
			this.setNull();
			return;
		}
		data=s;
		setASN1Type(Encoder.TAG_OCTET_STRING);
		setASN1Length(data.length);
		bytes+=data.length;
		assert(bytes==2+data.length);
	}
	/**
	 * Create string
	 * @param s
	 * @param ascii_vs_printable : if true IA5String else PrintableString
	 */
	public Encoder(String s, boolean ascii_vs_printable){
		data=s.getBytes(Charset.forName("US-ASCII"));
		if(ascii_vs_printable) setASN1Type(Encoder.TAG_IA5String);
		else setASN1Type(Encoder.TAG_PrintableString);
		setASN1Length(data.length);
		bytes+=data.length;
		assert(bytes==2+data.length);
	}
	/**
	 * String of type type
	 * @param s
	 * @param type
	 */
	public Encoder(String s, byte type){
		//if(s==null) s ="";
		data=s.getBytes(Charset.forName("US-ASCII"));
		setASN1Type(type);
		setASN1Length(data.length);
		bytes+=data.length;
		assert(bytes==2+data.length);
	}
	public static <T> Encoder getEncoder(T[] param){
		Encoder enc = new Encoder().initSequence();
		for(int k=0; k<param.length; k++) {
			enc.addToSequence(((ASNObj)param[k]).getEncoder());
		}
		return enc;
	}
	/**
	 * 
	 * @param param, an array of String[] to encode
	 * @param type, the type to assign to each String
	 * @return, an Encoder 
	 */
	public static Encoder getStringEncoder(String[] param,byte type){
		Encoder enc = new Encoder().initSequence();
		for(int k=0; k<param.length; k++) {
			enc.addToSequence(new Encoder(param[k],type));
		}
		return enc;
	}
	public Encoder setNull(){
		setASN1Type(Encoder.TAG_NULL);
		setASN1Length(0);
		assert(bytes==2);
		return this;
	}
	public void print(){
		System.err.println("Object size: "+bytes);
		System.err.println("typ:  "+Util.byteToHex(this.header_type," "));
		System.err.println("len:  "+Util.byteToHex(this.header_length," "));
		if(this.prefix_data!=null) this.prefix_data.print();
		System.err.println("data: "+Util.byteToHex(this.data," "));
		System.err.println("Done Object size: "+bytes);
	}
	public String toString(){
		//print();
		String res="";
		byte[] str = getBytes();
		return Util.byteToHex(str," ");
		//for(int k=0; k<str.length; k++) res=res+" "+str[k];
		//return res;
	}
	public static void main(String args[]) throws ASN1DecoderFail{
		Encoder seq2=(new Encoder())
					.initSequence().addToSequence(new Encoder((byte)3))
					.addToSequence(new Encoder(Calendar.getInstance()));
		//seq2.print();
		
		Encoder my_int = new Encoder((byte)124);
		Encoder my_int2 = new Encoder("1024");
		Encoder my_seq = (new Encoder()).initSequence()
			.addToSequence(my_int)
			.addToSequence(my_int2)
			.addToSequence(seq2)
			;
		System.out.println(my_seq);
		//my_seq.print();
		Decoder dec = new Decoder(my_seq.getBytes(),0);
		dec=dec.getContent();
		System.out.println(dec);
		System.err.println("124=: "+dec.getFirstObject(true).getInteger());
		System.out.println(dec);
		System.err.println("1024=: "+dec.getFirstObject(true).getString());
		System.out.println(dec);
		dec=dec.getContent();
		System.err.println("3=: "+dec.getFirstObject(true).getInteger());
		System.out.println(dec);
		System.err.println("now=: "+dec.getFirstObject(true).getGeneralizedTime());
		System.out.println(dec);
	}
}
