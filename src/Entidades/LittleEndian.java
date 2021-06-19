package Entidades;

public class LittleEndian {
	
	public static byte[] empaquetar(int n){
	
		byte b0 = (byte)((n >> 24) & 0xFF); 
		byte b1 = (byte)((n >> 16) & 0xFF); 
		byte b2 = (byte)((n >> 8) & 0xFF); 
		byte b3 = (byte)((n) & 0xFF); 
		
		byte[] arrayBytes = new byte[]{b3, b2, b1, b0};
		
		return arrayBytes;
	}

	public static int desempaquetar(byte[] arrayBytes){

		int i0 = ((arrayBytes[3] & 0xFF) << 24); 
		int i1 = ((arrayBytes[2] & 0xFF) << 16);
		int i2 = ((arrayBytes[1] & 0xFF) << 8);
		int i3 = ((arrayBytes[0] & 0xFF));

		int n = i0 | i1 | i2 | i3;
		
		return n;
	}
}
