package maow.asthacktest.core;

import maow.asthacktest.apt.annotation.GenerateFile;

@GenerateFile(name = "Meme.txt", text = "Meme School")
public final class Test {
    public static void main(String[] args) {
        Utils.invoke(Test.class,"writeFile");
    }
}
