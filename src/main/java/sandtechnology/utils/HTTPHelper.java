package sandtechnology.utils;

import sandtechnology.bilibili.POJOResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HTTPHelper{

   public enum State{
        Init,Success,BiliBiliError,NetworkError,Error
    }

    private final String url;
      private Consumer<POJOResponse> handler;
      private State state;
    private final Random random = new Random();

    public HTTPHelper(String url, Consumer<POJOResponse> handler){
          this.url=url;
          this.handler=handler;
          state=State.Init;
      }

    public void setHandler(Consumer<POJOResponse> handler) {
        this.handler = handler;
    }

    public State getState() {
        return state;
    }

    public Consumer<POJOResponse> getHandler() {
        return handler;
    }

    public void execute(){
        String result = "";
          try {
              URLConnection urlConnection = new URL(url).openConnection();
              urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0");
              try (BufferedReader stream = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                  result = stream.lines().collect(Collectors.joining("\n"));
              }
              POJOResponse pojoResponse = JsonHelper.getGsonInstance().fromJson(result, POJOResponse.class);
              if (pojoResponse.getCode() != 0) {
                  if (pojoResponse.getCode() != 600005 && pojoResponse.getCode() != -22) {
                      MessageHelper.sendingErrorMessage(new RuntimeException("Unexpected BiliBili Error:"+pojoResponse.getCode()),"content"+result);
                  }
                  state=State.BiliBiliError;
                  ThreadHelper.sleep(random.nextInt(10000) + 5000);
              }
              handler.accept(pojoResponse);
              state= State.Success;
          } catch (IOException e) {
              state = State.NetworkError;
              e.printStackTrace();
              MessageHelper.sendingErrorMessage(e, "Network Error:\n");
          } catch (Exception e) {
              state = State.Error;
              e.printStackTrace();
              MessageHelper.sendingErrorMessage(e, "Unknown Error:\ncontent:\n" + result);
          }
      }


    }