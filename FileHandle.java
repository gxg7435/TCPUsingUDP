
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class is implemented to read from the file and segment the data into
 * packets.
 * 
 * @author gaurav gaur(gxg7435@g.rit.edu)
 *
 */
public class FileHandle {

	public static String comp = "output.gz";
	public static String decomp = "final.jpg";

	/**
	 * This class is used to compress the original file using GZIPStream class.
	 * 
	 * @param filename:
	 *            input file
	 * @return : compressed file in .gz format
	 */
	public String compress(String filename) {

		byte[] buffer = new byte[65507];
		int len;

		try {

			FileInputStream fis = new FileInputStream(filename);
			GZIPOutputStream goz = new GZIPOutputStream(new FileOutputStream(comp));

			while ((len = fis.read(buffer)) > 0) {
				goz.write(buffer, 0, len);
			}

			fis.close();
			goz.finish();
			goz.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return comp;
	}

	/**
	 * This class is used to decompress the .gz file to final output file
	 * 
	 * @param filename:
	 *            .gz file
	 */
	public void decompress(String filename) {

		int len;
		byte[] buffer = new byte[65507];
		try {

			GZIPInputStream giz = new GZIPInputStream(new FileInputStream(filename));
			FileOutputStream fos = new FileOutputStream((decomp));

			while ((len = giz.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}

			giz.close();
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This class is used to read the data from compressed file and create the
	 * packets that needs to be sent to client.
	 */
	public HashMap<Integer, byte[]> segment() {

		File f = new File("output.gz");
		HashMap<Integer, byte[]> result = new HashMap<Integer, byte[]>();

		try {

			GZIPInputStream giz = new GZIPInputStream(new FileInputStream("output.gz"));
			int offset = 0, seq = 0;

			while (offset < f.length()) {

				byte[] temp = new byte[32700];
				seq++;

				giz.read(temp);
				result.put(seq, temp);
				offset += 32700;
			}
			byte[] left = new byte[(int) f.length() - (offset - 32700)];

			if (left.length > 0) {

				giz.read(left);
				result.put(seq + 1, left);

			}
			System.out.println("Total Packets Created : " + result.size());

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
}
