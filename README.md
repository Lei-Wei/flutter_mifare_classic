# mifare_classic

A new flutter plugin project.

I'm new to Flutter and Android Development so I don't intend to put this plugin to Dart Pub. This Plugin is mainly for reading specific Student Cards, so it's not that for common-use designed.

I would be happy if the source code could help you to deal with the MF1 card.

There are some places that you need to pay attention to:
1. in folder 'android', add the tech list and change manifest.
2. there are obviously some better ways to deal with 'value changed event' in dart which you don't need 'stream'.
3. I did write the keys in the plugin because my keys never change and the MF1 cards have already been cracked. you could write it in dart side and send it to the plugin. then you will need to set the MessageChannelHandler.
