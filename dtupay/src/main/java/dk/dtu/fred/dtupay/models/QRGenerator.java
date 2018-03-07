package dk.dtu.fred.dtupay.models;

import org.jgroups.util.UUID;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Generator for QR images encoding content.
 */
public class QRGenerator {
	
	/**
	 * Generate a QR image (stored as a {@link BitMatrix}) from a generated UUID.
	 * @param width resolution of the image (in pixels).
	 * @param height resolution of the image (in pixels).
	 * @return A {@link BitMatrix} representation of the encoded QR image.
	 * @throws WriterException if the QR image could not be encoded.
	 * @throws IllegalArgumentException if the size is lower than 1. 
	 */
	public static BitMatrix generateQRcode(int width, int height) throws WriterException {
		String randomNumber = UUID.randomUUID().toString();
				
		if (randomNumber == null || randomNumber.isEmpty()) {
			return null;
		}
		
		if (width < 1 || height < 1) {
			throw new IllegalArgumentException("Size of the QR code should be more than 1");
		}
		
		QRCodeWriter writer = new QRCodeWriter();
		return writer.encode(randomNumber, BarcodeFormat.QR_CODE, width, height);
	}
	
}
