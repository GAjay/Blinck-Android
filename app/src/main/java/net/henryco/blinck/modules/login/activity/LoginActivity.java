package net.henryco.blinck.modules.login.activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.facebook.login.widget.LoginButton;
import net.henryco.blinck.R;
import net.henryco.blinck.modules.BlinckApplication;
import net.henryco.blinck.modules.login.http.BlinckLoginService;
import net.henryco.blinck.util.retro.RetroCallback;

import javax.inject.Inject;

public class LoginActivity extends AppCompatActivity {

	private static final String[] permissions = {
			"user_birthday",
			"user_location",
			"user_likes",
			"user_education_history",
			"user_photos",
			"user_friends",
			"user_about_me",
			"read_custom_friendlists",
			"public_profile"
	};


	@Inject SharedPreferences sharedPreferences;
	@Inject BlinckLoginService loginService;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		((BlinckApplication) getApplication()).getLoginComponent().inject(this);

		loginService.getRequiredFacebookPermissionsList().enqueue(
				new RetroCallback<>((listCall, listResponse) -> {

				})
		);

		LoginButton fbLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
		fbLoginButton.setReadPermissions(permissions);
	}
}
