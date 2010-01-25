package example;

import com.natpryce.makeiteasy.Maker;

import static com.natpryce.makeiteasy.Maker.like;
import static com.natpryce.makeiteasy.Maker.make;
import static com.natpryce.makeiteasy.Maker.with;
import static example.builders.AppleMaker.anApple;
import static example.builders.AppleMaker.leaves;
import static example.builders.BananaMaker.aBanana;
import static example.builders.FruitMaker.ripeness;


@SuppressWarnings({"UnusedDeclaration"})
public class Example {
    public static void main(String[] args) {
        Maker<Apple> ripeApple = anApple(with(2, leaves), with(ripeness, 0.9));
        Maker<Apple> unripeApple = anApple(like(ripeApple), with(ripeness, 0.125));

        Apple apple1 = make(ripeApple);
        Apple apple2 = make(unripeApple);

        Banana defaultBanana = make(aBanana());
    }
}