
# Reactive API

The Reactive API is the base API put in place by ErisCasper.java.
It exposes events received from Discord as an `Observable<Event>`.

It has started as a RxJava wrapper around the JDA API.
As time goes on it will replace more of the JDA code base, with the
aim of being reactive all the way down.

## Example

Below is a ping and echo bot implemented using the Reactive API

```java
import com.github.princesslana.eriscasper.ErisCasper;
import com.github.princesslana.eriscasper.Message;

public class Main {
  public static void main(String[] args) throws Exception {
    ErisCasper ec = ErisCasper.create();

    Message.from(ec)
        .filter(m -> m.getContent().startsWith("+ping"))
        .subscribe(m -> m.reply("pong").subscribe());

    Message.from(ec)
        .filter(m -> m.getContent().startsWith("+echo"))
        .subscribe(m -> m.reply(m.getContent().substring(5)).subscribe());
  }
}
```
