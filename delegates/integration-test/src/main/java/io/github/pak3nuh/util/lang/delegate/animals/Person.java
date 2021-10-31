package io.github.pak3nuh.util.lang.delegate.animals;

public class Person implements WalkerDelegate, AnimalDelegate, BoxDelegate<Flyer> {

    private final Walker walker;
    private final Animal animal;
    private final Box<Flyer> birdCage;

    public Person(Walker walker, Animal animal, Box<Flyer> birdCage) {
        this.walker = walker;
        this.animal = animal;
        this.birdCage = birdCage;
    }

    @Override
    public Walker delegateTo(Walker caller) {
        return walker;
    }

    @Override
    public Animal delegateTo(Animal caller) {
        return animal;
    }

    @Override
    public Box<Flyer> delegateTo(Box<Flyer> caller) {
        return birdCage;
    }
}
