Build to simulate different types of automata for educational purposes. 

Since it's fairly obvious to visualize automata, I thought it might be useful to play through different events and get an idea of the formal definitions. The current state is limited to the very core features of placing states and connecting them. Now as the foundation is implemented actual automata related features can be implemented.

## How to use
1. Clone the repository.
2. First run the maven goals `kotlin:compile` and `javafx:compile`.
3. Start the application with `javafx:run`.
 
- Double-click to add a new state
- Right click drag to add a new edge
- Double-click on a state to mark it as final

#### Caveats:
- Mark the directory kotlin as source if you use Intellij, and it is not done automatically.
- You can also put all three in an IntelliJ run-configuration.
- Use Java11 or newer (however watch out for kotlin support).

## Project organization
- The project is written in **kotlin** and based on **JavaFx**.
- The logic and visualization is strictly seperated. There should be no dependencies from `logic` to any other package.
- As games were supposed to be supported at some point common code for automata and games is located in **common**.
- The main code is located in **automata**.
- The code under **games** is heavily WIP and currently not used.
- FXMl and CSS files can be found under *resources/com/jkrude/* and used to describe the UI.

https://user-images.githubusercontent.com/36801164/195395090-520ff7cc-c735-483e-891e-f624cade5ef5.mov

