/*
 Copyright 2024 European Commission

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package eu.assina.sa.pdf;

import eu.assina.csc.payload.RedirectLinkResponse;
import eu.assina.sa.client.ClientContext;
import eu.assina.sa.model.AssinaSigner;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

/**
 * Extracts content and adds signature to PDF
 * Taken from the pdfbox examples
 */
public class PdfSupport {

    private AssinaSigner signer;

    public PdfSupport(AssinaSigner contentSigner) {
        this.signer = contentSigner;
    }


    public RedirectLinkResponse getOIDRedirectLink(){
        return this.signer.getOIDRedirectLink();
    }
    

    public byte[] signPdfContent(InputStream content, ClientContext context) throws IOException, NoSuchAlgorithmException {
        byte[] pdfContentBytes = IOUtils.toByteArray(content);
//        writeToFile("assina_pdf_content", pdfContentBytes);
        byte[] signedContent = signer.signHash(pdfContentBytes, context);
//        writeToFile("assina_signed_content", signedContent);
        return signedContent;
    }

    /**
     * Signs the given PDF file.
     *
     * @param inFile  input PDF file
     * @param outFile output PDF file
     * @throws IOException if the input file could not be read
     */
    public void signDetached(File inFile, File outFile) throws IOException, NoSuchAlgorithmException {
        if (inFile == null || !inFile.exists()) {
            throw new FileNotFoundException("Document for signing does not exist");
        }

        FileOutputStream fos = new FileOutputStream(outFile);

        // sign
        PDDocument doc = null;
        try {
            doc = PDDocument.load(inFile);
            signDetached(doc, fos);
        } finally {
            IOUtils.closeQuietly(doc);
            IOUtils.closeQuietly(fos);
        }
    }

    public void signDetached(PDDocument document, OutputStream output) throws IOException, NoSuchAlgorithmException {
        int accessPermissions = getMDPPermission(document);
        if (accessPermissions == 1) {
            throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
        }

        // create signature dictionary
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);

        // the signing date, needed for valid signature
        signature.setSignDate(Calendar.getInstance());

        // Optional: certify
        if (accessPermissions == 0) {
            setMDPPermission(document, signature, 2);
        }

        ClientContext context = signer.prepCredential();

        signature.setName(context.getSubject());
        signature.setLocation("Braga, Portugal");
        signature.setReason("Signing documents with Assina");

        document.addSignature(signature);
        ExternalSigningSupport externalSigning = document.saveIncrementalForExternalSigning(output);
        // invoke external signature service
        final InputStream content = externalSigning.getContent();
//        InputStream newContent = forkToFile("assina_sa_unsigned_pdf_content", content);
        byte[] cmsSignature = signPdfContent(content, context);

        // set signature bytes received from the service
        externalSigning.setSignature(cmsSignature);
    }

    // Utilities for permissions Copied from the Apache Pdf-Box examples

    /**
     * Get the access permissions granted for this document in the DocMDP transform parameters
     * dictionary. Details are described in the table "Entries in the DocMDP transform parameters
     * dictionary" in the PDF specification.
     *
     * @param doc document.
     * @return the permission value. 0 means no DocMDP transform parameters dictionary exists. Other
     * return values are 1, 2 or 3. 2 is also returned if the DocMDP transform parameters dictionary
     * is found but did not contain a /P entry, or if the value is outside the valid range.
     */
    public static int getMDPPermission(PDDocument doc) {
        COSBase base = doc.getDocumentCatalog().getCOSObject().getDictionaryObject(COSName.PERMS);
        if (base instanceof COSDictionary) {
            COSDictionary permsDict = (COSDictionary) base;
            base = permsDict.getDictionaryObject(COSName.DOCMDP);
            if (base instanceof COSDictionary) {
                COSDictionary signatureDict = (COSDictionary) base;
                base = signatureDict.getDictionaryObject(COSName.REFERENCE);
                if (base instanceof COSArray) {
                    COSArray refArray = (COSArray) base;
                    for (int i = 0; i < refArray.size(); ++i) {
                        base = refArray.getObject(i);
                        if (base instanceof COSDictionary) {
                            COSDictionary sigRefDict = (COSDictionary) base;
                            if (COSName.DOCMDP.equals(sigRefDict.getDictionaryObject(COSName.TRANSFORM_METHOD))) {
                                base = sigRefDict.getDictionaryObject(COSName.TRANSFORM_PARAMS);
                                if (base instanceof COSDictionary) {
                                    COSDictionary transformDict = (COSDictionary) base;
                                    int accessPermissions = transformDict.getInt(COSName.P, 2);
                                    if (accessPermissions < 1 || accessPermissions > 3) {
                                        accessPermissions = 2;
                                    }
                                    return accessPermissions;
                                }
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Set the "modification detection and prevention" permissions granted for this document in the
     * DocMDP transform parameters dictionary. Details are described in the table "Entries in the
     * DocMDP transform parameters dictionary" in the PDF specification.
     *
     * @param doc               The document.
     * @param signature         The signature object.
     * @param accessPermissions The permission value (1, 2 or 3).
     * @throws IOException if a signature exists.
     */
    public static void setMDPPermission(PDDocument doc, PDSignature signature, int accessPermissions) throws IOException {
        for (PDSignature sig : doc.getSignatureDictionaries()) {
            // "Approval signatures shall follow the certification signature if one is present"
            // thus we don't care about timestamp signatures
            if (COSName.DOC_TIME_STAMP.equals(sig.getCOSObject().getItem(COSName.TYPE))) {
                continue;
            }
            if (sig.getCOSObject().containsKey(COSName.CONTENTS)) {
                throw new IOException("DocMDP transform method not allowed if an approval signature exists");
            }
        }

        COSDictionary sigDict = signature.getCOSObject();

        // DocMDP specific stuff
        COSDictionary transformParameters = new COSDictionary();
        transformParameters.setItem(COSName.TYPE, COSName.TRANSFORM_PARAMS);
        transformParameters.setInt(COSName.P, accessPermissions);
        transformParameters.setName(COSName.V, "1.2");
        transformParameters.setNeedToBeUpdated(true);

        COSDictionary referenceDict = new COSDictionary();
        referenceDict.setItem(COSName.TYPE, COSName.SIG_REF);
        referenceDict.setItem(COSName.TRANSFORM_METHOD, COSName.DOCMDP);
        referenceDict.setItem(COSName.DIGEST_METHOD, COSName.getPDFName("SHA1"));
        referenceDict.setItem(COSName.TRANSFORM_PARAMS, transformParameters);
        referenceDict.setNeedToBeUpdated(true);

        COSArray referenceArray = new COSArray();
        referenceArray.add(referenceDict);
        sigDict.setItem(COSName.REFERENCE, referenceArray);
        referenceArray.setNeedToBeUpdated(true);

        // Catalog
        COSDictionary catalogDict = doc.getDocumentCatalog().getCOSObject();
        COSDictionary permsDict = new COSDictionary();
        catalogDict.setItem(COSName.PERMS, permsDict);
        permsDict.setItem(COSName.DOCMDP, signature);
        catalogDict.setNeedToBeUpdated(true);
        permsDict.setNeedToBeUpdated(true);
    }

    /*
    public static String TEST_HASH =
            "0�\u0006\t*�H��\n" + "\u0001\u0007\u0002��0�\u0002\u0001\u00011\u000F0\n" +
                    "\u0006\t`�H\u0001e\u0003\u0004\u0002\u0001\u0005\u00000�\u0006\t*�H��\n" +
                    "\u0001\u0007\u0001��$�\u00043\u0011;�\u0006�+!l�;\b���h�\u00026ȜP�\u001F�q0۶�)\u007F\u0000\u0000\u0000\u0000\u0000\u0000��0�\u0002�0�\u0001��\u0003\u0002\u0001\u0002\u0002\u0006\u0001zNoA�0\n" +
                    "\u0006\t*�H��\n" +
                    "\u0001\u0001\u000B\u0005\u00000\u00141\u00120\u0010\u0006\u0003U\u0004\u0003\f\tassina.eu0\u001E\u0017\n" +
                    "210627170552Z\u0017\n" +
                    "220627170552Z0\u00111\u000F0\n" +
                    "\u0006\u0003U\u0004\u0003\f\u0006carlos0�\u0001\"0\n" +
                    "\u0006\t*�H��\n" +
                    "\u0001\u0001\u0001\u0005\u0000\u0003�\u0001\u000F\u00000�\u0001\n" +
                    "\u0002�\u0001\u0001\u0000�{�4�[=�~�\"yQ��;�*K��Ƴ5\"?�\u001A���7�g\u0014$�\u000E_w���,�=a�%�O4�Og\u0000���oQ\u0002�\u0012H'\n" +
                    "��%=�I\f�Ȱ'5s���30�n�#.�N?J���;��\\H\u000F���0膓-\u001D�\u0019\u0006\u0006A\u0003�O�㣽��#��S�H�;\u001D�Q�\u05EE���¾ ���TFO��\u0019�@M�>�%\u0013�N���ߧ�\u001E�5\u001E�>��YRZV�hT���xS�wwҁp�)��܀�^T�;\u0001M�MHu�T�f%\u0003e8W�]���aw͡ӥFm�va�/7x\u0018/��\u000Fs�\u0002\u0003\u0001\u0000\u0001�\u00130\u00110\u000F\u0006\u0003U\u001D\u0013\u0001\u0001�\u0004\u00050\u0003\u0001\u0001�0\n" +
                    "\u0006\t*�H��\n" +
                    "\u0001\u0001\u000B\u0005\u0000\u0003�\u0001\u0001\u0000\u0018�q\u0015���F�)b��:\u001F�J(T�)��\n" +
                    "ϧ�_Yc���`����H�c�B������\u000B_�>\u0019?P\u0007v)\u001C['@�@G��0�3����\u0015K�3sSO��.�U�\u000B����;\u001A�ȕ�J��1�\"�6�||&��\u001E�\u000B�Q\u0010E�a�[^�+��(\u0006�t��ѧm�Г>��v��3�8�>�\u0006\u001D:�\u001F�\u0011<�E\u0012�ʳ�ڜ�A_6�?|� ��k?\u001C\\\tM��1\"���wI��B'`�\u001B\"U�$�hɵT.i���P\u000B`������`�\u0011cI�f�\u000F\u0013��Yv]\u000F���\b\u0006\u000F�\u0010\u0000\u00001�\u0001�0�\u0001�\u0002\u0001\u00010\u001E0\u00141\u00120\u0010\u0006\u0003U\u0004\u0003\f\tassina.eu\u0002\u0006\u0001zNoA�0\n" + "\u0006\t`�H\u0001e\u0003\u0004\u0002\u0001\u0005\u0000���0\u0018\u0006\t*�H��\n" + "\u0001\t\u00031\u000B\u0006\t*�H��\n" + "\u0001\u0007\u00010\u001C\u0006\t*�H��\n" + "\u0001\t\u00051\u000F\u0017\n" + "210628121147Z0-\u0006\t*�H��\n" + "\u0001\t41 0\u001E0\n" + "\u0006\t`�H\u0001e\u0003\u0004\u0002\u0001\u0005\u0000�\n" + "\u0006\t*�H��\n" + "\u0001\u0001\u000B\u0005\u00000/\u0006\t*�H��\n" + "\u0001\t\u00041\"\u0004 ����\tp�aյcј\u000B��^�\u0004���{�O������.0\n" + "\u0006\t*�H��\n" + "\u0001\u0001\u000B\u0005\u0000\u0004�\u0001\u0000(�)�#B9�X\t�\n" + "\t�\u0011+W��o;��ϊ\u0019,wx*������ �������l�K\u0013vY�,�3s�r\\�2v`�/\u0005�t�~\u0005\u0007T\"t\u0005[h�ڲ-j۳ �������v\u000E�$�\u007F\tb(쑫Qz#�\n" + "Ï�@z_+����a0\u001C>1��t�(�,�����E�\u007F\u058C!����\uF4DA�n�r�\n" +
                    "�-0�< A�G��)\u0001�k�S\t�\u001A�?\u000F�f\n" +
                    "F��\u0019}t����\u0005���k��\u0017�MFVqڠ\n" +
                    "�eݗ%��8�gc�69�$�`KC�\b:�ּ�l �i�\n" +
                    "\u0000��Q9HMQ۽^�\u0000\u0000\u0000\u0000\u0000\u0000";


    public static void main(String[] args) throws IOException, GeneralSecurityException {
        PdfSupport signing = new PdfSupport(new AssinaSigner() {
            @Override
            public ClientContext prepCredential() {
                ClientContext context = new ClientContext();
                context.setSignAlgo("");
                context.setSubject("test subject");
                return context;
            }

            @Override
            public byte[] signHash(byte[] pdfHash, ClientContext context) {
                return TEST_HASH.getBytes(StandardCharsets.UTF_8);
            }
        });

        File inFile = new File(args[0]);
        String name = inFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));

        File outFile = new File(inFile.getParent(), substring + "_signed.pdf");
        signing.signDetached(inFile, outFile);
    }

    // For testing only
    public static void writeToFile(String name, byte[] bytes) {
        Path dir = Paths.get("/Users/cwerner/um/src/lei/");
        Path path = dir.resolve(name);
        try {
            Files.write(path, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // for testing only
    public static ByteArrayInputStream forkToFile(String name, InputStream input) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048]; // you can configure the buffer size
            int length;
            while ((length = input.read(buffer)) != -1) out.write(buffer, 0, length); //copy streams
            input.close(); // call this in a finally block
            byte[] result = out.toByteArray();
            writeToFile(name, result);
            return new ByteArrayInputStream(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }*/
}
