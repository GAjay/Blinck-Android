package net.henryco.blinck.groups.login.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import lombok.val;
import net.henryco.blinck.R;
import net.henryco.blinck.BlinckApplication;
import net.henryco.blinck.configuration.BlinckServerAPI;
import net.henryco.blinck.groups.login.broker.FacebookLoginBroker;
import net.henryco.blinck.groups.login.service.BlinckLoginService;
import net.henryco.blinck.groups.main.activity.MainPageActivity;
import net.henryco.blinck.util.form.login.UserLoginForm;
import net.henryco.blinck.util.form.login.UserStatusForm;
import net.henryco.blinck.util.function.BlinckBiConsumer;
import net.henryco.blinck.util.reflect.AutoFind;
import net.henryco.blinck.util.reflect.AutoFinder;
import net.henryco.blinck.util.retro.RetroCallback;
import retrofit2.Call;
import retrofit2.Response;

import javax.inject.Inject;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

	private static final int CONNECTION_ATTEMPTS_MAX_NUMB = 5;
	private volatile int connection_attempt_numb;

	private FacebookLoginBroker facebookLoginBroker;

	@Inject SharedPreferences sharedPreferences;
	@Inject BlinckLoginService loginService;

	@AutoFind(R.id.fb_login_button)
	private LoginButton loginButton;


	private final BlinckBiConsumer<Call<List<String>>, Response<List<String>>>
			onResponse = (listCall, listResponse) -> {

		List<String> list = listResponse.body();
		onGetPermissionsSuccess_1(list == null
				? new String[] {
					"user_birthday",
					"user_location",
					"user_likes",
					"user_education_history",
					"user_photos",
					"user_friends",
					"user_about_me",
					"read_custom_friendlists",
					"public_profile"
				}
				: list.toArray(new String[0])
		);
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_login);
		((BlinckApplication) getApplication()).getLoginComponent().inject(this);
		AutoFinder.find(this);

		this.connection_attempt_numb = 0;
		tryToConnect();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebookLoginBroker.onActivityResult(requestCode, resultCode, data);
	}



	private void tryToConnect() {

		if (connection_attempt_numb++ < CONNECTION_ATTEMPTS_MAX_NUMB) {
			loginService.getRequiredFacebookPermissionsList()
					.enqueue(new RetroCallback<>(onResponse));
		} else {
			Toast.makeText(this, "Looks like there is connection troubles", Toast.LENGTH_LONG).show();
			tryToEnterToMainPage();
		}
	}



	private void onGetPermissionsSuccess_1(String ... permissions) {

		final AccessToken accessToken = AccessToken.getCurrentAccessToken();
		loginButton.setReadPermissions(permissions);

		facebookLoginBroker = new FacebookLoginBroker(loginButton, CallbackManager.Factory.create());
		facebookLoginBroker.onSuccess(loginResult -> onFacebookAuthSuccess_2(loginResult.getAccessToken()));
		facebookLoginBroker.activate();

		if (accessToken != null && !accessToken.isExpired()) {
			facebookLoginBroker.disableLoginButton();
			onFacebookAuthSuccess_2(accessToken);
		}
		else facebookLoginBroker.reset();
	}



	private void onFacebookAuthSuccess_2(AccessToken accessToken) {

		UserLoginForm form = new UserLoginForm(accessToken.getUserId(), accessToken.getToken());
		loginService.postLoginForm(form)
				.enqueue(new RetroCallback<>((call, response) ->
						onAppAuthSuccess_3(response.headers().get(BlinckServerAPI.HttpHeaders.AUTHORIZATION))
				, (voidCall, throwable) -> tryToConnect())
		);
	}



	private void onAppAuthSuccess_3(String app_token) {

		loginService.getUserStatus(app_token).enqueue(new RetroCallback<>((call, response) -> {

			UserStatusForm status = response.body();
			if (status != null && status.getActive())
				onGetStatusSuccess_4(status.getPrincipal(), app_token);
			else {
				facebookLoginBroker.reset();
				tryToConnect();
			}
		},(userStatusFormCall, throwable) -> tryToConnect()));
	}



	private void onGetStatusSuccess_4(String userId, String app_token) {

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(getString(R.string.preference_app_token), app_token);
		editor.putString(getString(R.string.preference_app_uid), userId);
		editor.apply();

		tryToEnterToMainPage();
	}



	private void tryToEnterToMainPage() {

		val uid = sharedPreferences.getString(getString(R.string.preference_app_uid), null);
		if (uid != null && !uid.isEmpty()) {
			startActivity(new Intent(this, MainPageActivity.class));
			finish();
		}
	}


}