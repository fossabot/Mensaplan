package de.dotwee.rgb.canteen.model.api.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.dotwee.rgb.canteen.model.constant.Location;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import timber.log.Timber;

/**
 * Created by lukas on 12.11.2016.
 */
public class CacheHelper {
    public static final String FILENAME_FORMAT = "%s-%d.csv";
    private static final String TAG = CacheHelper.class.getSimpleName();

    public static Location[] getCached(@NonNull File cacheDir, int weeknumber) {
        List<Location> locationList = new ArrayList<>();

        for (File file : cacheDir.listFiles()) {
            for (Location location : Location.values()) {
                String filename = String.format(Locale.getDefault(), FILENAME_FORMAT, location.getNameTag(), weeknumber);
                if (file.getName().equalsIgnoreCase(filename)) {
                    locationList.add(location);
                }
            }
        }

        Location[] locations = new Location[locationList.size()];
        locations = locationList.toArray(locations);
        return locations;
    }

    public static void clear(@NonNull File cacheDir) {
        for (File file : cacheDir.listFiles()) {
            boolean deletion = file.delete();

            if (!deletion) {
                Timber.e("File with path=%s couldn't be deleted", file.getAbsolutePath());
            }
        }
    }

    static void persist(@NonNull File cacheDir, @NonNull InputStream inputStream, @NonNull String filename) {
        Timber.i("persisting file=%s", filename);

        OutputStream fileOutputStream = null;
        File file = new File(cacheDir, filename);

        try {
            fileOutputStream = new FileOutputStream(file);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, read);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            Timber.e(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Timber.e(e);
                }
            }
        }
    }

    @Nullable
    public static InputStream read(@NonNull File cacheDir, @NonNull String filename) throws FileNotFoundException {
        Timber.i("reading file=%s", filename);

        return new FileInputStream(new File(cacheDir, filename));
    }

    public static boolean exists(@NonNull File cacheDir, @NonNull String filename) {
        return new File(cacheDir, filename).exists();
    }

    @Deprecated
    public static boolean exists(@NonNull File cacheDir, int weeknumber) {
        boolean state = true;

        for (Location location : Location.values()) {
            File file = new File(String.format(Locale.getDefault(), FILENAME_FORMAT, location.getNameTag(), weeknumber));
            state = state == file.exists();
        }

        return state;
    }

    @NonNull
    public static Observable<Void> getObservable(final int weeknumber, @NonNull final File cacheDir) {
        return Observable.create(new ObservableOnSubscribe<Void>() {
            @Override
            public void subscribe(ObservableEmitter<Void> e) throws Exception {
                for (Location location : Location.values()) {
                    Timber.i("Executing %s for location=%s weeknumber=%d", TAG, location.getNameTag(), weeknumber);

                    String filename = String.format(Locale.getDefault(), FILENAME_FORMAT, location.getNameTag(), weeknumber);
                    if (!CacheHelper.exists(cacheDir, filename)) {
                        String URL_FORMAT = "http://www.stwno.de/infomax/daten-extern/csv/%s/%s.csv";

                        URL url = new URL(String.format(Locale.getDefault(), URL_FORMAT, location.getNameTag(), String.valueOf(weeknumber)));
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.connect();

                        if (httpURLConnection.getResponseCode() == 200) {
                            InputStream inputStream = httpURLConnection.getInputStream();
                            CacheHelper.persist(cacheDir, inputStream, filename);
                            e.onNext(null);
                        } else {
                            e.onError(new IllegalStateException("Connection code is " + httpURLConnection.getResponseCode() + "; can't save stream"));
                        }

                        httpURLConnection.disconnect();
                    }

                    e.onError(new IllegalStateException("File " + filename + " already exsits"));
                }
            }
        });
    }
}
