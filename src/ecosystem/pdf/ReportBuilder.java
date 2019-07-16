/*
 * WARNING: THIS FILE MAY BLIND YOU OR IN THE BEST CASE CAUSE SEIZURES, PLEASE BEWARE!
 */

package ecosystem.pdf;

import ecosystem.Ecosystem;
import static ecosystem.Ecosystem.*;
import ecosystem.Financial;
import ecosystem.academic.Subject;
import ecosystem.user.AcademicData;
import ecosystem.user.Student;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class ReportBuilder {
    private static final int LEFT_ALIGN = 0;
    private static final int CENTER_ALIGN = 1;
    private static final int RIGHT_ALIGN = 2;

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

    private static String cornyNumber(float number) {
        String num = Float.toString(number), result = "";
        String[] parts = num.split("\\.");
        String integerPart = parts[0], fractionalPart = parts[1];

        for (int i = 0, len = integerPart.length(); i < len; i++) {
            result += integerPart.charAt(i);
            if ((len - i) % 3 == 1 && i < (len - 1))
                result += '.';
        }

        result += "," + fractionalPart;
        return result;
    }

    private static String cornyDate() {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    private static void drawColumns(PDPageContentStream contents, String[] content, int[] halign, float[] widths, float w, float lm, float rm, float cy, float height, PDFont font, int fontSize) throws IOException {
        float currentX = lm;
        for (int i = 0; i < content.length; i++) {
            String text = content[i];
            float tw = getStringWidth(text, font, fontSize);
            float th = getFontHeight(font, fontSize);
            float cw = (i < content.length - 1) ? widths[i] : w - rm - currentX;
            float cx = currentX;
            boolean thingsChangedYKnow = false;

            while (tw > cw - 7) { // too awkward to be real
                while (tw > cw - 14) {
                    text = text.substring(0, text.length() - 1);
                    tw = getStringWidth(text + "...", font, fontSize);
                    thingsChangedYKnow = true;
                }
            }

            if (thingsChangedYKnow)
                text += "...";

            float ctx = 0;
            if (halign[i] == LEFT_ALIGN)
                ctx = cx + 7;
            else if (halign[i] == CENTER_ALIGN)
                ctx = cx + (cw - tw) / 2;
            else if (halign[i] == RIGHT_ALIGN)
                ctx = cx + cw - tw - 7;

            float cty = cy + (height - th) / 2;
            contents.addRect(cx, cy, cw, height);
            contents.stroke();
            contents.beginText();
            contents.setFont(font, fontSize);
            contents.newLineAtOffset(ctx, cty);
            contents.showText(text);
            contents.endText();
            currentX += cw;
        }
    }

    public static void buildPaymentReport(AcademicData academic, File file) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        PDImageXObject image = PDImageXObject.createFromFile(getResourcePath("/images/ecosystem.png"), document);
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
        float rx = rmargin, y = iy - leading - rh;
        float t3x = rx + (rw - t3w) / 2, t3y = y + (rh - t3h) / 2;
        contents.addRect(rx, y, rw, rh);
        contents.stroke();
        contents.beginText();
        contents.setFont(f2, fs2);
        contents.newLineAtOffset(t3x, t3y);
        contents.showText(text3);
        contents.endText();

        float ch = 20;
        String studId = academic.getStudentId();
        Student stud = (Student) userManager.get(studId);

        drawColumns(contents, new String[] {
            "Fecha: " + cornyDate(),
            "Identificación: " + studId,
            "Estudiante: " + stud
        }, new int[] { CENTER_ALIGN, LEFT_ALIGN, LEFT_ALIGN }, new float[] { 100, 150 }, w, rmargin, rmargin, y -= ch, ch, f2, 10);

        y -= ch;
        drawColumns(contents, new String[] {
            "Código",
            "Materia",
            "Créditos",
            "Valor a pagar ($)"
        }, new int[] { CENTER_ALIGN, CENTER_ALIGN, CENTER_ALIGN, CENTER_ALIGN }, new float[] { 60, 300, 60 }, w, rmargin, rmargin, y -= ch, ch, f2, 10);

        float total = 0;
        for (String sid : academic.subjects) {
            Subject s = subjectManager.get(sid);
            float cost = Financial.getSubjectCost(s);
            total += cost;

            drawColumns(contents, new String[] {
                sid,
                s.getName(),
                Integer.toString(s.getCredits()),
                cornyNumber(cost)
            }, new int[] { CENTER_ALIGN, LEFT_ALIGN, CENTER_ALIGN, CENTER_ALIGN }, new float[] { 60, 300, 60 }, w, rmargin, rmargin, y -= ch, ch, f2, 10);
        }

        y -= ch / 2;
        drawColumns(contents, new String[] {
            "Total",
            "$" + total
        }, new int[] { CENTER_ALIGN, CENTER_ALIGN }, new float[] { 60 }, w, 410, rmargin, y -= ch, ch, f2, 10);

        contents.close();
        document.addPage(page);
        document.save(file);
    }

    public static void buildFinancialReport(File file) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        PDImageXObject image = PDImageXObject.createFromFile(getResourcePath("/images/ecosystem.png"), document);
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

        String text3 = "Reporte financiero (Fecha de generación: " + cornyDate() + ")";
        float t3w = getStringWidth(text3, f2, fs2);
        float t3h = getFontHeight(f2, fs2);
        int leading = 20, rmargin = 50;
        float rw = w - 2 * rmargin, rh = 25;
        float rx = rmargin, y = iy - leading - rh;
        float t3x = rx + (rw - t3w) / 2, t3y = y + (rh - t3h) / 2;
        contents.addRect(rx, y, rw, rh);
        contents.stroke();
        contents.beginText();
        contents.setFont(f2, fs2);
        contents.newLineAtOffset(t3x, t3y);
        contents.showText(text3);
        contents.endText();

        float ch = 20;
        drawColumns(contents, new String[] {
            "Código",
            "Materia",
            "#",
            "Creditos",
            "Valor recibido ($)"
        }, new int[] { CENTER_ALIGN, CENTER_ALIGN, CENTER_ALIGN, CENTER_ALIGN, CENTER_ALIGN }, new float[] { 60, 275, 35, 50 }, w, rmargin, rmargin, y -= ch, ch, f2, 10);

        float total = 0;
        for (Subject s : subjectManager.list) {
            float profit = Financial.getSubjectCost(s) * s.getStudentCount();
            total += profit;

            drawColumns(contents, new String[] {
                s.getId(),
                s.getName(),
                Integer.toString(s.getStudentCount()),
                Integer.toString(s.getCredits()),
                Float.toString(profit)
            }, new int[] { CENTER_ALIGN, LEFT_ALIGN, CENTER_ALIGN, CENTER_ALIGN, CENTER_ALIGN }, new float[] { 60, 275, 35, 50 }, w, rmargin, rmargin, y -= ch, ch, f2, 10);
        }

        y -= ch / 2;
        drawColumns(contents, new String[] {
            "Total",
            "$" + total
        }, new int[] { CENTER_ALIGN, CENTER_ALIGN }, new float[] { 50 }, w, 420, rmargin, y -= ch, ch, f2, 10);

        contents.close();
        document.addPage(page);
        document.save(file);
    }

    public static void main(String[] args) throws IOException {
        Ecosystem.initDataManagers();
        buildFinancialReport(new File("document.pdf"));
        /*Student juan = (Student) userManager.get("1");
        buildPaymentReport(juan.getAcademicData(), new File("document.pdf"));*/
    }
}
