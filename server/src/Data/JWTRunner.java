package server.src.Data;

import com.google.gson.JsonObject;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JWTRunner {
    //ExpiryToken length is set to be 24 hours in ms
    private final long EXPIRYTOKENLENGTH = 24*60*60*1000;
    private final String ALGOTYPE = "SHA-256";
    private final String KEY = "NirWoo";
    private MessageDigest messageDigest;
    public JsonObject Alg;
    public JsonObject User;

    public JWTRunner() throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(ALGOTYPE);
        Alg = new JsonObject();
        User = new JsonObject();
    }

    //Encodes a "token" by the format : Base64(Algorithm Type+Token type).Base64(UserName+ ExpTime).Sha256(Base64(PrivateKey))
    public String encode(String userName) throws IOException, NoSuchAlgorithmException{
        setJsonProps(userName);
        String AlgEncoded = org.apache.commons.codec.binary.Base64.encodeBase64String(Alg.toString().getBytes()).toString();
        String UserEncoded = org.apache.commons.codec.binary.Base64.encodeBase64String(User.toString().getBytes());
        StringBuilder jsonsCombiner = new StringBuilder();
        jsonsCombiner.append(AlgEncoded);
        jsonsCombiner.append(".");
        jsonsCombiner.append(UserEncoded);
        String dataCheckSum = checksum(KEY);
        dataCheckSum = org.apache.commons.codec.binary.Base64.encodeBase64String(dataCheckSum.getBytes());
        jsonsCombiner.append(".");
        jsonsCombiner.append(dataCheckSum);
        return String.valueOf(jsonsCombiner);
    }
    //Decodes the "token" from format: Base64(Algorithm Type+Token type).Base64(UserName+ ExpTime).Sha256(Base64(PrivateKey))
    public String decode(String encodedData) {
        String[] threePartsEncoding = encodedData.split("\\.");
        if(threePartsEncoding.length!=3)return null;
        String algDecoded = null;
        try {
            algDecoded = new String( org.apache.commons.codec.binary.Base64.decodeBase64(threePartsEncoding[0]),"UTF8");
            String userDecoded = new String(org.apache.commons.codec.binary.Base64.decodeBase64(threePartsEncoding[1]),"UTF8");
            String hashDecoded = new String(org.apache.commons.codec.binary.Base64.decodeBase64(threePartsEncoding[2]),"UTF8");
            return algDecoded + "."+userDecoded+"."+hashDecoded;  }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;

    }


    private void setJsonProps(String userName){
        long time =System.currentTimeMillis();
        time += EXPIRYTOKENLENGTH;
        Alg.addProperty("alg","HS256");
        Alg.addProperty("type","JWT");
        User.addProperty("name",userName);
        User.addProperty("exp", time);
    }
    //Hashing the secret for the token
    private  String checksum(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGOTYPE);
        String hex = (new HexBinaryAdapter()).marshal(md.digest(data.getBytes()));
        return hex;
    }
}
