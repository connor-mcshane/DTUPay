package dk.dtu.fred.dtupay;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import dk.dtu.fred.dtupay.models.QRGenerator;

public class QRGeneratorTest {
	
	/**
	 * Test of uniqueness of QRcodes created by the method {@link dk.dtu.fred.application.core.generateQRcode()}.
	 */
	@Test
	public void testGenerateQRCodesAreUnique() throws WriterException {
		BitMatrix qrCode1 = QRGenerator.generateQRcode(115, 115);
		BitMatrix qrCode2 = QRGenerator.generateQRcode(115, 115);
		assertNotEquals(qrCode1.toString(), qrCode2.toString());
	}

	/**
	 * Test for method {@link dk.dtu.fred.application.core.generateQRcode()} with negative sizes.
	 * @throws IllegalArgumentException
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstructorNegativeSize() throws WriterException {
		BitMatrix actual = QRGenerator.generateQRcode(-1, 115);
		assertNull(actual);
		
		actual = QRGenerator.generateQRcode(115, -1);
		assertNull(actual);
	}
	
	/*@Test
	public void generateQRCode_image() throws WriterException, IOException {
		BitMatrix qrCode1 = QRgenerator.generateQRcode(300, 300);
		QRgenerator.createQRcodeImage(qrCode1, "qrcode300.png");
	}*/

}
