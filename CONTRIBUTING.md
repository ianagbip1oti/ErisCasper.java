# Contributing

There are many ways to contribute to ErisCasper.java.
This is true whether you are interested in working on the core library code, developing a bot using ErisCasper.java,
or not even a developer at all.
For example:

 * [Create an issue](https://github.com/princesslana/ErisCasper.java/issues/new) for a bug report or feature request
 * Contribute to the documentation
   - [For the site](https://github.com/princesslana/ErisCasper.java/tree/master/src/site)
   - Javadoc 
 
## Contributing Code

If you're unsure where to get started, here are some starting points:

  * Issues labeled as
    [Good First Issue](https://github.com/princesslana/ErisCasper.java/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22)
    or [Help Wanted](https://github.com/princesslana/ErisCasper.java/issues?q=is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22+label%3A%22help+wanted%22)
  * Checkout the [Sonarcloud dashboard](https://sonarcloud.io/dashboard?id=com.github.princesslana%3AErisCasper.java)
    for any technical debt reported and submit a fix
 
### Before you get started

Ready to jump in? Great!

Before you get started on a task:
  * Have you run a quick search of issues and PRs to see if something similar has been suggested or submitted before?
    This doesn't mean you shouldn't attempt this task, but previous discussion could raise issues you'd like
    to address with your work.
  * Multiple PRs with small and targeted improvements are preferable to large changes.
    Smaller PRs are easier to review, so usually get merged quicker.
    If you can break your work down into multiple PRs, then do it.
 
### IDE

ErisCasper.java is IDE agnostic.

The project is maven based. Use any IDE that supports maven projects.

### Style

Code style is checked on builds by using [Spotless](https://github.com/diffplug/spotless).
Running `mvn spotless:apply` will format the code to be compliant with this.
It is recommended you run this before creating a Pull Request.

If you wish to have your IDE confirm to the format, check out the plugins for
[google-java-format](https://github.com/google/google-java-format).
 
### Code Quality

[Sonarcloud](https://sonarcloud.io) is used to monitor code quality.
The dashboard for ErisCasper.java is located
[here](https://sonarcloud.io/dashboard?id=com.github.princesslana%3AErisCasper.java).
It does not currently give feedback on PRs, but it can be useful to know the kinds of issues it reports.

## Finally

If you have any questions or comments, or anything else really, please reach out to [The Programmer's Hangout](https://discord.gg/programming) and look for Princess Lana.

