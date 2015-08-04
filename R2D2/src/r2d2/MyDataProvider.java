package r2d2;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/*
* Annotation for feeding arguments to methods conforming to the
* "@DataProvider" annotation type. Its a custom DataProvider
*/


@Retention(RetentionPolicy.RUNTIME)
public @interface MyDataProvider {
	  /*
	��* String array of key-value pairs fed to a dynamic data provider.
	��* Should be in the form of key=value, e.g. args={"foo=bar", "biz=baz"}
	��*/

	String value();
}


