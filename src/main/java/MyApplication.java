import com.google.gson.annotations.SerializedName;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.*;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by tommy.li on 27/3/2018.
 */
public class MyApplication {
    private static CompositeDisposable compositeDisposable = new CompositeDisposable();
    public static void main(String[] arg){

        getNodeAPI().test().subscribeOn(Schedulers.io()).filter(responseData -> responseData.getStatus()!=400).subscribe(getObserver());

        new MyApplication().waitMethod();
    }

    private static int counter = 0;
    private synchronized void waitMethod() {
        while (true) {
            System.out.println(counter+" always running program ==> " + Calendar.getInstance().getTime());
            try {
                this.wait(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public interface NodeAPI{
        @GET("test")
        Observable<ResponseData> test();

        @GET("test")
        Observable<ResponseData> testQuery(@Query("id_mapping")int id_mapping);
    }

    public static Observer getObserver(){
        return new Observer<ResponseData>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(ResponseData responseData) {
                System.out.println(responseData.getMessage());
                if(responseData.next!=null)
                    getNodeAPI().testQuery(responseData.next).subscribeOn(Schedulers.io()).filter(response -> response.getStatus()!=400).subscribe(getObserver());
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("completed");
            }
        };
    }

    public static class ResponseData {
        @SerializedName("message")
        private String message;
        @SerializedName("status")
        private int status;

        @SerializedName("params")
        private Object params;
        @SerializedName("body")
        private Object body;
        @SerializedName("query")
        private HashMap<String,String> query;
        @SerializedName("next")
        private Integer next;

        public String getMessage() {
            return message;
        }

        public int getStatus() {
            return status;
        }
    }

    public static NodeAPI getNodeAPI(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://thawing-river-44581.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(NodeAPI.class);
    }
}
