package mozrt.hsmw.de;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;

/*
---------------
MOZRT.java
---------------
(C) Copyright 2015.

Original Author:  Dirk Pawlaszczyk
Contributor(s):   -;


Project Info:  http://www.hs-mittweida.de

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

Dieses Programm ist Freie Software: Sie können es unter den Bedingungen
der GNU General Public License, wie von der Free Software Foundation,
Version 3 der Lizenz oder (nach Ihrer Wahl) jeder neueren
veröffentlichten Version, weiterverbreiten und/oder modifizieren.

Dieses Programm wird in der Hoffnung, dass es nützlich sein wird, aber
OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
Siehe die GNU General Public License für weitere Details.

Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.

*/
/**
 * Core application class. It is meant to recover lost SQLite records from a
 * sqlite database file. As a carving tool, it is binary based and can recover 
 * deleted entries from a mozilla places-db-file as well as other sqlite 3.x 
 * database files. 
 * 
 * From the sqlite web-page:
 * "A database file might contain one or more pages that are not in active use. 
 * Unused pages can come about, for example, when information is deleted from 
 * the database. Unused pages are stored on the freelist and are reused when 
 * additional pages are required."
 * 
 * This class makes use of this behavior.
 * 
 * Note: The tool does not support forensic analysis of unallocated space (at
 * least not in the moment ;-)). 
 * 
 * @author Dirk Pawlaszczyk
 * @version 1.0
 */
public class MOZRT {

	static String path;
	static RandomAccessFile db = null;
	final static String MAGIC_HEADER_STRING = "53514c69746520666f726d6174203300";
	final static String NO_AUTO_VACUUM = "00000000";
	final static String NO_MORE_ENTRIES = "00000000";
	final static String DATA_PAGE = "0d";
	final static String ROW_ID = "00";
	final protected static char[] hexArray = "0123456789abcdef".toCharArray();
	static Charset db_encoding = StandardCharsets.UTF_8;
	static List<String> lines = new LinkedList<String>();
	static int scanned_entries = 0;
	static int places_idx = 0;
	static MOZRT_UI gui = null;
	static Hashtable<String, ArrayList<String[]>> tables = new Hashtable<String, ArrayList<String[]>>();
	static Hashtable<String, String> tblSig;
	static boolean is_default = false;

	private static void processDB() {
		boolean repeat = false;

		try {

			db = new RandomAccessFile(new File(path), "r");

			/********************************************************************/
			db.seek(0);
			byte header[] = new byte[16];
			db.read(header);
			if (bytesToHex(header).equals(MAGIC_HEADER_STRING)) // we currently
																// support
																// sqlite 3 data
																// bases
				log("header is okay. seems to be an sqlite database file.");
			else {
				log("sorry. doesn't seem to be an sqlite file. wrong header.");
				err("Doesn't seem to be an valid sqlite file. Wrong header");
				return;
			}

			/********************************************************************/

			db.seek(0);
			byte startpage[] = new byte[65536];
			db.read(startpage);
			ByteBuffer bb = ByteBuffer.wrap(startpage);
			if (Util.contains(bb, "moz_places")) {
				is_default = false;
				log("found table >>moz_places<<.");
				if (null != gui)
					gui.prepare_moz_places_tables();

				tblSig = SchemaLoader.readTableFingerprints("/moz_places_tbl.info");
				Hashtable<String, String[]> heads = SchemaLoader
						.readTableHeaders("/moz_places_header.info");

				// System.out.println(tblSig);
				log(heads.toString());
			} else {
				is_default = true;
				tblSig = new Hashtable<String, String>();
				tblSig.put("", "default");
				log("found unkown sqlite-database.");
				Hashtable<String, String[]> heads = new Hashtable<String, String[]>();
				heads.put("default", new String[] { "unkown", "unkown",
						"unkown", "unkown", "unkown", "unkown", "unkown",
						"unkown", "unkown", "unkown", "unkown" });

				if (null != gui)
					gui.prepare_table_default();
			}

			/********************************************************************/

			db.seek(52);
			byte autovacuum[] = new byte[4];
			db.read(autovacuum);
			if (bytesToHex(autovacuum).equals(NO_AUTO_VACUUM)) {
				log("Seems to be no AutoVacuum db. Nice :-).");
			}

			/********************************************************************/
			// Determine database text encoding.
			// A value of 1 means UTF-8. A value of 2 means UTF-16le. A value of
			// 3 means UTF-16be.
			db.seek(56);
			byte[] encoding = new byte[4];
			db.read(encoding);
			ByteBuffer enc = ByteBuffer.wrap(encoding);
			int codepage = enc.getInt();
			switch (codepage) {
			case 1:
				db_encoding = StandardCharsets.UTF_8;
				log("Database encoding: " + "UTF_8");
				break;

			case 2:
				db_encoding = StandardCharsets.UTF_16LE;
				log("Database encoding: " + "UTF_16LE");
				break;

			case 3:
				db_encoding = StandardCharsets.UTF_16BE;
				log("Database encoding: " + "UTF_16BE");
				break;
			}

			/*******************************************************************/

			db.seek(16);
			byte pagesize[] = new byte[2];
			db.read(pagesize);
			ByteBuffer size = ByteBuffer.wrap(pagesize);
			short v = size.getShort(); // first determine the signed short value
			int ps = v >= 0 ? v : 0x10000 + v; // we have to convert an unsigned
												// short value to an integer

			log("page size " + ps + " Bytes ");

			/*******************************************************************/

			db.seek(36);
			byte freepageno[] = new byte[4];
			db.read(freepageno);
			log("Total number of free list pages " + bytesToHex(freepageno));

			/*******************************************************************/

			db.seek(32);
			byte freelistpage[] = new byte[4];
			db.read(freelistpage);
			log("FreeListPage starts at byte " + bytesToHex(freelistpage));
			ByteBuffer freelistoffset = ByteBuffer.wrap(freelistpage);
			int head = freelistoffset.getInt();
			log("head:: " + head);
			int start = (head - 1) * ps; // should be 4096 for an mozilla
							 			 // db-file

			/*******************************************************************/

			if (head == 0) {
				err("Couldn't locate any free pages to recover. Sorry.");
				return;
			}

			db.seek(start);
			log("first:: " + start + " 0hx " + Integer.toHexString(start));

			// seeking file pointer to the first free page entry
			db.seek(start);

			do {
				// now read the first 4 Byte to get the next free page index
				byte nextfp[] = new byte[4];
				db.read(nextfp);

				// example : 00 00 15 3C | 00 00 02 2B

				byte numberOfEntries[] = new byte[4];
				db.read(numberOfEntries);
				ByteBuffer e = ByteBuffer.wrap(numberOfEntries);
				int entries = e.getInt();
				log(" Number of Entries " + entries);

				for (int zz = 0; zz < entries; zz++) {
					// head of free page list + i*4
					db.seek(start + 8 + (4 * zz));
					byte next[] = new byte[4];
					db.read(next);
					ByteBuffer buffer = ByteBuffer.wrap(next);
					int n = buffer.getInt();

					// determine offset for free page
					int offset = (n - 1) * ps;

					log("entry:: " + n + " offset :: " + offset);

					// goto free page start
					if (offset < 0)
						continue;
					db.seek(offset);

					// test for index or data page, skip index pages
					byte pagetype = (byte) db.read();
					// page type index or data_page ?
					if (byteToHex(pagetype).equals(DATA_PAGE)) // we currently
																// support
																// sqlite 3 data
																// bases
					{
						// Found Data-Page
						// Determine number of cell pointers
						db.seek(offset + 3); // offset 4+5 of this page
						byte cpn[] = new byte[2];
						db.read(cpn);
						size = ByteBuffer.wrap(cpn);
						v = size.getShort(); // first determine the signed short
												// value
						int cp = v >= 0 ? v : 0x10000 + v; // we have to convert
															// an unsigned short
															// value to an
															// integer
						log(" number of cells: " + cp);

						for (int i = 0; i < cp; i++) {

							// address of the next cell pointer
							db.seek(offset + 8 + 2 * i);
							byte pointer[] = new byte[2];
							db.read(pointer);
							ByteBuffer address = ByteBuffer.wrap(pointer);
							short v2 = address.getShort(); // first determine
															// the signed short
															// value
							int p = v2 >= 0 ? v2 : 0x10000 + v2; // we have to
																	// convert
																	// an
																	// unsigned
																	// short
																	// value to
																	// an
																	// integer

							// go to record begin
							db.seek(offset + p);

							// length of payload
							int pll = readUnsignedVarInt();
                            pll = pll + 1;
							int rowid = readUnsignedVarInt();

							// now read the payload header length, again a
							// varint-value
							int phl = readUnsignedVarInt();

							SqliteElement[] columns = getColumns(phl - 1);
							 System.out.println(java.util.Arrays.toString(columns));

							String lineUTF = "";
							String[] row = new String[20];
							row[0] = Integer.toString(rowid);
							int co = 1;
							String fp = Util.getTableFingerPrint(columns);
							String tblname = Signatures.getTable(fp);

							for (SqliteElement en : columns) {
								byte[] value = new byte[en.length];
								db.read(value);
								if ((en.type != SqliteRawType.PRIMARY_KEY)
										|| co > 2) {
									String val = en
											.toString(value, db_encoding);
									row[co] = val;
									lineUTF += ";" + val;
									co++;
								}

							}

							lines.add(lineUTF);
                            System.out.println(".. " +lineUTF);
							
							if (!tables.containsKey(tblname))
								tables.put(tblname, new ArrayList<String[]>());

							ArrayList<String[]> r = tables.get(tblname);

							r.add(row);

						}
					} else {
						//System.out.println(" idx ");
					}

				}
				scanned_entries += entries;

				if (!bytesToHex(nextfp).equals(NO_MORE_ENTRIES)) {
					ByteBuffer of = ByteBuffer.wrap(nextfp);
					int nfp = of.getInt();
					int continuewith = (nfp - 1) * ps; // should be 4096 for an
														// mozilla db-file
					db.seek(continuewith);
					repeat = true;
				} else
					repeat = false;

			} while (repeat); // while

			log(" Finished. No further free pages.");

			if (gui != null) {
				Enumeration<String> values = tables.keys();
				while (values.hasMoreElements()) {
					String tbln = values.nextElement();
					
					gui.update_table(tbln, tables.get(tbln));
				}
			}

			// Path textFile = Paths.get("test.txt");
			// Files.write(textFile, lines,
			// StandardCharsets.UTF_8,StandardOpenOption.CREATE);

			db.close();

		} catch (IOException e) {

			log("Error: Could not open file.");
			System.exit(-1);
		}
	}

	private static void printOptions() {
		System.out.println(" ");
		System.out.println("Usage: MOZRT class [-options] ");
		System.out.println("where possible options include: ");
		System.out.println("   <filename>  path to sqlite db-file ");
	}

	static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];

		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	private static String byteToHex(byte b) {
		byte[] ch = new byte[1];
		ch[0] = b;
		return bytesToHex(ch);
	}

	static boolean testBit(int n, int pos) {
		int mask = 1 << pos;

		return (n & mask) == mask;
		// alternative: return (n & 1<<pos) != 0;
	}

	static int clearBit(int n, int pos) {
		return n & ~(1 << pos);
	}

	public static long readUnsignedVarLong(RandomAccessFile in)
			throws IOException {

		long value = 0L;
		long b;

		// values are big endian! - We need to consider the order of bytes
		while (((b = in.readByte()) & 0x80L) != 0) {
			value |= (b & 0x7F) << 7;
		}
		return value | b;
	}

	public static int readUnsignedVarInt() throws IOException {
		int value = 0;
		int b;

		// long start = db.getFilePointer();

		// as long as we have a byte with most significant bit value 1
		// there are more byte to read
		while (((b = db.readByte()) & 0x80) != 0) {

			value |= (b & 0x7F) << 7;
		}
		// long end = db.getFilePointer();
		// System.out.println("Varint :: " + (end - start ) + " Bytes ");
		return value | b;
	}

	public static int[] readVarInt(byte[] values) {
		int value = 0;
		int counter = 0;
		byte b = 0;

		int[] result = new int[values.length];

		while (counter < values.length) {

			b = values[counter];
			value = 0;
			while (((b = values[counter]) & 0x80) != 0) {

				value |= (b & 0x7F) << 7;
				counter++;
			}
			result[counter] = value | b;
			counter++;
		}
		return result;
	}

	public static SqliteElement[] getColumns(int headerlength)
			throws IOException {

		byte[] header = new byte[headerlength];

		// get header information
		db.read(header);
		// System.out.println(bytesToHex(header));

		// there are serveral varint values in the payload header
		int[] columns = readVarInt(header);
		SqliteElement[] column = new SqliteElement[columns.length];

		for (int i = 0; i < columns.length; i++) {

			switch (columns[i]) {
			case 0: // primary key
				column[i] = new SqliteElement(SqliteRawType.PRIMARY_KEY, 0);
				break;
			case 1: // 8bit complement integer
				column[i] = new SqliteElement(SqliteRawType.INT8, 1);
				break;
			case 2: // 16bit integer
				column[i] = new SqliteElement(SqliteRawType.INT16, 2);
				break;
			case 3: // 24bit integer
				column[i] = new SqliteElement(SqliteRawType.INT24, 3);
				break;
			case 4: // 32bit integer
				column[i] = new SqliteElement(SqliteRawType.INT32, 4);
				break;
			case 5: // 48bit integer
				column[i] = new SqliteElement(SqliteRawType.INT48, 6);
				break;
			case 6: // 64bit integer
				column[i] = new SqliteElement(SqliteRawType.INT48, 8);
				break;
			case 7: // Big-endian floating point number
				column[i] = new SqliteElement(SqliteRawType.FLOAT64, 8);
				break;
			case 8: // Integer constant 0
				columns[i] = 0;
				break;
			case 9: // Integer constant 1
				columns[i] = 0;
				break;
			case 10: // not used ;

			case 11:
				columns[i] = 0;
				break;

			default:
				if (columns[i] % 2 == 0) // even
				{
					// BLOB with the length (N-12)/2
					column[i] = new SqliteElement(SqliteRawType.BLOB,
							(columns[i] - 12) / 2);
				} else // odd
				{
					// String in database encoding (N-13)/2
					column[i] = new SqliteElement(SqliteRawType.STRING,
							(columns[i] - 13) / 2);
				}

			}

		}

		return column;
	}

	protected static void setGUI(MOZRT_UI gui) {
		MOZRT.gui = gui;
	}

	public static void setPath(String file) {
		path = file;
	}

	public static void start() {
		if (path != null)
			processDB();
		else
			return;
	}

	static void log(String message) {
		if (gui != null)
			gui.doLog(message);
		else
			System.out.println(message);
	}

	static void err(String message) {
		if (gui != null)
			JOptionPane.showMessageDialog(gui, message);
		else
			System.err.println("ERROR: " + message);
	}

	/**
     * You can use this class as a command line tool as well. 
     * 
	 * @param args
	 */
	public static void main(String[] args) {

		System.out
				.println("**************************************************************");
		System.out
				.println("* Mozilla free page recovery Tool (MOZRT)                    *");
		System.out
				.println("*                                               version: 1.0 *");
		System.out
				.println("* Author: D. Pawlaszczyk                                     *");
		System.out
				.println("**************************************************************");

		if (args.length == 0)
			printOptions();
		else {
			path = args[0];
			processDB();
		}

	}

}

enum SqliteRawType {
	PRIMARY_KEY, INT8, INT16, INT24, INT32, INT48, INT64, FLOAT64, INT0, INT1, NOTUSED1, NOTUSED2, BLOB, STRING
}

class Util {

	static String getTableFingerPrint(SqliteElement[] columns) {
		String fp = "";

		for (SqliteElement e : columns)
			fp += e.type;
		return fp;
	}

	static boolean contains(ByteBuffer bb, String searchText) {
		String text = new String(bb.array());
		if (text.indexOf(searchText) > -1)
			return true;
		else
			return false;
	}

}

class SqliteElement {
	SqliteRawType type;
	int length;

	SqliteElement(SqliteRawType type, int length) {
		this.length = length;
		this.type = type;
	}

	final String toString(byte[] value, Charset encoding) {
		if (value.length == 0)
			return "";
		if (type == SqliteRawType.INT8)
			return "" + decodeInt8(value[0]);
		if (type == SqliteRawType.INT16)
			return "" + decodeInt16(value);
		if (type == SqliteRawType.INT24)
			return "" + decodeInt24(value);
		if (type == SqliteRawType.INT32)
			return "" + decodeInt32(value);
		if (type == SqliteRawType.INT48)
			return "" + decodeInt48(value);
		if (type == SqliteRawType.INT64)
			return "" + decodeInt64(value);
		if (type == SqliteRawType.FLOAT64)
			return "" + decodeFloat64(value);
		if (type == SqliteRawType.BLOB)
			return "" + MOZRT.bytesToHex(value);
		return decodeString(value, encoding);
	}

	final static int decodeInt8(byte v) {
		return v;
	}

	final static int decodeInt16(byte[] v) {
		ByteBuffer bf = ByteBuffer.wrap(v);
		return bf.getShort();
	}

	final static int decodeInt24(byte[] v) {
		// this is a little bit buggy, actually we have 3 bytes to read
		ByteBuffer bf = ByteBuffer.wrap(v);
		return bf.getShort();
	}

	final static int decodeInt32(byte[] v) {
		ByteBuffer bf = ByteBuffer.wrap(v);
		return bf.getInt();
	}

	final static String decodeInt48(byte[] v) {
		ByteBuffer bf = ByteBuffer.wrap(v);
		// bf.order(ByteOrder.BIG_ENDIAN);
		long z = bf.getLong();
		if (z > 1000000000000000L && z < 1800000000000000L)
			return convertToDate(z);
		else
			return Long.toString(z);
	}

	final static String decodeInt64(byte[] v) {

		ByteBuffer bf = ByteBuffer.wrap(v);
		long z = bf.getLong();
		if (z > 100000000000L)
			return convertToDate(z);
		return Long.toString(z);
	}

	final static String convertToDate(long value) {

		Date d = new Date(value / 1000);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd/MM/yyyy hh:mm:ss");
		return dateFormat.format(d);
	}

	final static double decodeFloat64(byte[] v) {
		ByteBuffer bf = ByteBuffer.wrap(v);
		return bf.getDouble();
	}

	final static String decodeString(byte[] v, Charset encoding) {
		Charset charset = Charset.forName(encoding.name());
		CharBuffer data = charset.decode(ByteBuffer.wrap(v));
		return data.toString();
	}

}

class Signatures {

	static String getTable(String signature) {

		if (MOZRT.is_default)
			return "default";

		signature = signature.trim();

		signature = signature.replaceAll("[0-9]", "");

		signature = signature.replaceAll("PRIMARY_KEY", "");

		String tblname = MOZRT.tblSig.get(signature);

		if (null == tblname) {
			MOZRT.log("Unknown Table fingerprint : " + signature);
			return "";
		}

		return tblname;
		/*
		 * switch (signature){
		 * 
		 * case "INTINTINTINTINT" : return "moz_history_visits";
		 * 
		 * case "INTINTINTINTINTINT" : return "moz_history_visits";
		 * 
		 * case "INTINTINTSTRINGINTINT" : return "moz_bookmarks_roots";
		 * 
		 * case "STRINGSTRINGSTRINGINTINTINTINTINT" : return "moz_places";
		 * 
		 * case "STRINGSTRINGSTRINGINTINTINTINTINTINT" : return "moz_places";
		 * 
		 * case "INTINTINTINTSTRINGINTINT": return "moz_bookmarks";
		 * 
		 * case "STRING" : return "moz_anno_attributes";
		 * 
		 * case "STRINGSTRINGSTRINGINTINTINTINT" : return "moz_places";
		 * 
		 * case "STRINGBLOBSTRINGINT" : return "moz_favicons";
		 * 
		 * default: MOZRT.log("Unknown Table fingerprint : " + signature); }
		 * return "";
		 */

	}
}
