package mozrt.hsmw.de;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.ImageIcon;

/*
 ---------------
 SchemaLoader.java
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
 * Offers some utility-methods for loading db-shema descriptions. All table
 * names are normally stored in simple text based files.
 * 
 * @author Dirk Pawlaszczyk
 *
 */
class SchemaLoader {

	protected static Hashtable<String, String[]> readTableHeaders(
			String filename) {

		Hashtable<String, String[]> headers = new Hashtable<String, String[]>();

		try {
			URL url = MOZRT_UI.class.getResource(filename);			
		    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			//RandomAccessFile raf = new RandomAccessFile(filename, "r");			
			String header = in.readLine();

			while (header != null) {
				header = header.trim();
				if ((header.length() == 0) || (header.startsWith("#"))) {
					header = in.readLine();
					continue;
				}

				String[] entry = header.split(":");
				String tblname = entry[0];
				String[] columns = Arrays.copyOfRange(entry, 1,
						entry.length - 1);

				headers.put(tblname, columns);
				header = in.readLine();
				// System.out.println(".");
				System.out.println(headers);
			}
			in.close();

		}

		catch (Exception e) {
			System.err.println(" Error while loading table signatures.");
			e.printStackTrace();
		}

		return headers;
	}

	protected static Hashtable<String, String> readTableFingerprints(
			String filename) {

		Hashtable<String, String> signatures = new Hashtable<String, String>();

		try {
			//RandomAccessFile raf = new RandomAccessFile(filename, "r");
			URL url = MOZRT_UI.class.getResource(filename);			
		    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			String tbldef = in.readLine();

			tbldef = tbldef.trim();
			while (tbldef != null) {
				if ((tbldef.length() == 0) || (tbldef.startsWith("#"))) {
					tbldef = in.readLine();
					continue;
				}
				String[] entry = tbldef.split(":");
				if (entry.length >= 2) {
					String tblname = entry[0];
					String tblfp = entry[1];
					signatures.put(tblfp, tblname);
				}

				tbldef = null;
				tbldef = in.readLine();
			}
			in.close();

		}

		catch (Exception e) {
			System.err.println(" Error while loading table signatures.");
			e.printStackTrace();
		}

		return signatures;
	}

}
