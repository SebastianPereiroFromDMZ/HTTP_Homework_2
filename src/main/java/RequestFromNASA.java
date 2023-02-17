import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.internal.runners.statements.Fail;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class RequestFromNASA {

    public static ObjectMapper mapper = new ObjectMapper();


    public static void main(String[] args) throws IOException {


        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();

        HttpGet request = new HttpGet("https://api.nasa.gov/planetary/apod?api_key=GBkGbbfvpykZZM5lhzDnoJgdBQZeKflYfuHgm7hf");

        CloseableHttpResponse response = httpClient.execute(request);

        String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

        /**
         * Можно еще вопрос?При работе с API jackson-databind почемуто не смог преобразовать файл ответа с сервера в обьект java.
         * Использовал код перевода:
         *
         * ArrayList<Information> information = mapper.readValue(
         *                 response.getEntity().getContent(),
         *                 new TypeReference<>() {
         *                 }
         *         );
         *
         *         Вылетает ошибка Exception in thread "main" com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot deserialize instance of `java.util.ArrayList<Information>` out of START_OBJECT token
         *  at [Source: (org.apache.http.client.entity.LazyDecompressingInputStream); line: 1, column: 1]
         *
         *  Гуглил так и не нашол информации.
         *  Получилось через API gson.
         *  Подскажите где мой косяк? Как перевести через джексон?
         *  Спасибо :-)
         */

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Information information = gson.fromJson(body, Information.class);

        response.close();

        String address = information.getHdurl();

        String[] words = address.split("/");
        String fileName = words[words.length - 1];

        HttpGet requestHdurl = new HttpGet(address);//запрос

        CloseableHttpResponse responseHdurl = httpClient.execute(requestHdurl);//ответ с сервера

        OutputStream out = new FileOutputStream(fileName);

        byte[] in = responseHdurl.getEntity().getContent().readAllBytes();//записываем ответ  в массив байт

        out.write(in);//записываем файл локально


        responseHdurl.close();
        out.close();
        httpClient.close();

//Другой вариант скачивания и сохранения картинки
//        try {
//            URL url = new URL(address);
//            InputStream in = url.openStream();
//            OutputStream out = new FileOutputStream(fileName);
//            byte[] buffer = new byte[2048];
//
//            int length = 0;
//
//            while ((length = in.read(buffer)) != -1){
//                out.write(buffer, 0, length);
//            }
//
//            in.close();
//            out.close();
//
//        }catch (Exception e){
//            System.out.println(e.getMessage());
//        }
    }
}
