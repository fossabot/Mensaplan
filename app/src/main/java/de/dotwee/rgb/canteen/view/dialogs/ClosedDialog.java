package de.dotwee.rgb.canteen.view.dialogs;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialog;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.dotwee.rgb.canteen.R;
import de.dotwee.rgb.canteen.view.activities.SplashActivity;

/**
 * Created by lukas on 30.12.2016.
 */
public class ClosedDialog extends AppCompatDialog {
    private static final String TAG = ClosedDialog.class.getSimpleName();

    @BindView(R.id.buttonExit)
    Button buttonExit;

    @BindView(R.id.buttonRefresh)
    Button buttonRefresh;

    SplashActivity splashActivity;

    public ClosedDialog(@NonNull SplashActivity splashActivity) {
        super(splashActivity, R.style.AppTheme_Dialog_Closed);
        this.splashActivity = splashActivity;

        setContentView(R.layout.dialog_closed);
        ButterKnife.bind(this, getWindow().getDecorView());
    }

    @OnClick(R.id.buttonRefresh)
    public void onClickRefresh() {
        splashActivity.recreate();
    }

    @OnClick(R.id.buttonExit)
    public void onClickExit() {
        splashActivity.finishAffinity();
    }
}