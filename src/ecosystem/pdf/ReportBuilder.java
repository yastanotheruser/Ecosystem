package ecosystem.pdf;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class ReportBuilder {
    private static String getResourcePath(String path) {
        return System.getProperty("user.dir") + "/" + path;
    }

    public static File getResource(String path) {
        return new File(getResourcePath(path));
    }

    private static float getStringWidth(String text, PDFont font, int fontSize) throws IOException {
        return font.getStringWidth(text) / 1000 * fontSize;
    }

    private static float getFontHeight(PDFont font, int fontSize) throws IOException {
        return font.getFontDescriptor().getAscent() / 1000 * fontSize;
    }

    public static void main(String[] args) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        PDImageXObject image = PDImageXObject.createFromFile(getResourcePath("src/images/ecosystem.png"), document);
        PDPageContentStream contents = new PDPageContentStream(document, page);
        PDType0Font f1 = PDType0Font.load(document, getResource("trebucbd.ttf"));
        PDType0Font f2 = PDType0Font.load(document, getResource("trebuc.ttf"));
        PDRectangle rect = page.getMediaBox();

        String text1 = "Ecosystem";
        String text2 = "Sistema de matriculas";
        int fs1 = 22, fs2 = 12;
        float t1w = getStringWidth(text1, f1, fs1);
        float t1h = getFontHeight(f1, fs1);
        float t2w = getStringWidth(text2, f1, fs2);
        float t2h = getFontHeight(f1, fs2);
        int hgap = 10, vgap = 5, margin = 20;

        float w = rect.getWidth(), h = rect.getHeight();
        int iw = image.getWidth(), ih = image.getHeight();
        float ix = (w - iw - t1w - hgap) / 2;
        float iy = h - margin - ih;
        float t1x = ix + iw + hgap, t1y = iy + (ih + vgap - t1h + t2h) / 2;
        float t2x = t1x, t2y = t1y - vgap - t2h;

        contents.drawImage(image, ix, iy);
        contents.beginText();
        contents.setFont(f1, fs1);
        contents.newLineAtOffset(t1x, t1y);
        contents.showText(text1);
        contents.endText();
        contents.beginText();
        contents.setFont(f1, fs2);
        contents.newLineAtOffset(t2x, t2y);
        contents.showText(text2);
        contents.endText();

        String text3 = "Reporte de pago";
        float t3w = getStringWidth(text3, f2, fs2);
        float t3h = getFontHeight(f2, fs2);
        int leading = 20, rmargin = 50;
        float rw = w - 2 * rmargin, rh = 25;
        float rx = rmargin, ry = iy - leading - rh;
        contents.addRect(rx, ry, rw, rh);
        contents.stroke();
        contents.beginText();
        contents.setFont(f2, fs2);
        contents.newLineAtOffset(rx + (rw - t3w) / 2, ry + (rh - t3h) / 2);
        contents.showText(text3);
        contents.endText();

        String text4 = "Código";
        int fs3 = 10;
        float t4w = getStringWidth(text4, f2, fs3);
        float t4h = getFontHeight(f2, fs3);
        float ch = 20;
        float c1m = 5, c1w = t4w + 2 * c1m;
        float c1x = rmargin, c1y = ry + ch;
        contents.addRect(c1x, c1y, c1w, rh);
        contents.stroke();
        contents.close();
        document.addPage(page);
        document.save(new File("document.pdf"));
    }
}
