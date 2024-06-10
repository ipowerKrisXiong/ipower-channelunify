package com.ipower.cloud.channelunify.infra.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 画制定logo和制定描述的二维码
 *
 * @author xl
 */
public class QrCodeUtil {

    /**
     * 默认是黑色
     */
    private static final int QRCOLOR = 0xFF000000;
    /**
     * 背景颜色
     */
    private static final int BGWHITE = 0xFFFFFFFF;
    /**
     * 二维码宽
     */
    private static final int WIDTH = 300;
    /**
     * 二维码高
     */
    private static final int HEIGHT = 300;

    /**
     * 用于设置QR二维码参数
     */
    public static Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>() {
        private static final long serialVersionUID = 1L;

        {
            // 设置QR二维码的纠错级别（H为最高级别）具体级别信息
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            // 设置编码方式
            put(EncodeHintType.CHARACTER_SET, "utf-8");
            put(EncodeHintType.MARGIN, 1);
        }
    };


//    public static void main(String[] args)
//            throws IOException
//    {

//        System.out.println("支持的字体-------------");
//        Font[] fonts = GraphicsEnvironment
//                .getLocalGraphicsEnvironment().getAllFonts();
//        for (Font f : fonts) {
//            System.out.println("Name:" + f.getFontName());
//        }
//        System.out.println("支持的字体-------------");

//        File logoFile = new File("D://1.jpg");
//        BufferedImage outImage =  QrCodeUtil.getBufferedImage(logoFile,"1234567890","A001 扫码点单");
//        // 二维码输出文件
//        File file = new File("D://qrcode.png");
//        ImageIO.write(outImage, "png", file);
//    }


    /**
     * 生成带logo的二维码图片
     * 因为用了Graphics2D绘图，
     * 要注意部署的系统是否支持字体，目前是写死的宋体用于绘制二维码下标文字。一般win支持但是linux不支持，linux的话就需要安装好宋体字体，否则图片回乱码
     * https://blog.csdn.net/csdn2193714269/article/details/118306713
     *
     * @param logoFile logo图片地址
     * @param qrText
     */
    public static BufferedImage getBufferedImage(File logoFile, String qrText, String bottomText) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            BitMatrix bm = multiFormatWriter.encode(qrText, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    image.setRGB(x, y, bm.get(x, y) ? QRCOLOR : BGWHITE);
                }
            }
            int width = image.getWidth();
            int height = image.getHeight();
            //设置logo图
            if (Objects.nonNull(logoFile) && logoFile.exists()) {
                // 读取Logo图片
                BufferedImage logo = ImageIO.read(logoFile);
                logo = setRadius(logo, width / 2);
                // 构建绘图对象
                Graphics2D g = image.createGraphics();
                // 开始绘制logo图片
                g.drawImage(logo, width * 2 / 5, height * 2 / 5, width * 2 / 10, height * 2 / 10, null);
                g.dispose();
                logo.flush();
            }

            //设置二维码底下文字
            if (Objects.nonNull(bottomText)) {
                // 新的图片，把带logo的二维码下面加上文字
                int textHeight = 26;
                int textMargin = 10;
                BufferedImage outImage = new BufferedImage(width, height + textHeight + (textMargin * 2), BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D outg = outImage.createGraphics();
                // 画二维码到新的面板
                outg.drawImage(image, 0, 0, height, height, null);
                outg.setFont(new Font("宋体", Font.BOLD, 26)); // 字体、字型、字号
                int strWidth = outg.getFontMetrics().stringWidth(bottomText);
                outg.setColor(Color.BLACK);
                outg.drawString(bottomText, (outImage.getWidth() - strWidth) / 2, outImage.getHeight() - textMargin);
                outg.dispose();
                outImage.flush();
                return outImage;
            } else {
                image.flush();
                return image;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 图片设置圆角
     *
     * @param image 图片流
     * @return radius 半径
     * //     * @throws IOException
     */
    public static BufferedImage setRadius(BufferedImage image, int radius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return output;
    }

    /**
     * 设置图片文本
     *
     * @param image
     * @param height
     * @param note
     * @return
     */
    public static BufferedImage setImgText(BufferedImage image, int height, String note) {
        // 自定义文本描述
        if (!StringUtils.isEmpty(note)) {
            // 新的图片，把带logo的二维码下面加上文字
            BufferedImage outImage = new BufferedImage(400, 445, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D outg = outImage.createGraphics();
            // 画二维码到新的面板
            outg.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
            // 画文字到新的面板
            outg.setColor(Color.BLACK);
            // 字体、字型、字号
            outg.setFont(new Font("宋体", Font.BOLD, 20));
            int strWidth = outg.getFontMetrics().stringWidth(note);
            if (strWidth > 399) {
                // 长度过长就换行
                String note1 = note.substring(0, note.length() / 2);
                String note2 = note.substring(note.length() / 2, note.length());
                int strWidth1 = outg.getFontMetrics().stringWidth(note1);
                int strWidth2 = outg.getFontMetrics().stringWidth(note2);
                outg.drawString(note1, 200 - strWidth1 / 2, height + (outImage.getHeight() - height) / 2 + 12);
                BufferedImage outImage2 = new BufferedImage(400, 485, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D outg2 = outImage2.createGraphics();
                outg2.drawImage(outImage, 0, 0, outImage.getWidth(), outImage.getHeight(), null);
                outg2.setColor(Color.BLACK);
                // 字体、字型、字号
                outg2.setFont(new Font("宋体", Font.BOLD, 30));
                outg2.drawString(note2, 200 - strWidth2 / 2, outImage.getHeight() + (outImage2.getHeight() - outImage.getHeight()) / 2 + 5);
                outg2.dispose();
                outImage2.flush();
                outImage = outImage2;
            } else {
                // 画文字
                outg.drawString(note, 200 - strWidth / 2, height + (outImage.getHeight() - height) / 2 + 12);
            }
            outg.dispose();
            outImage.flush();
            image = outImage;
        }
        return image;
    }

}
