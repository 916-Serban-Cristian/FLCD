import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FiniteAutomaton {
    private List<String> states;
    private List<String> alphabet;
    private String initialState;
    private List<String> outputStates;
    private List<Transition> transitions;

    public FiniteAutomaton(String filename) {
        try {
            var reader = new BufferedReader(new FileReader(filename));
            reader.lines().forEach(line -> {
                var pattern = Pattern.compile("^([a-z_]*)=");
                var matcher = pattern.matcher(line);
                if (matcher.find()) {
                    var key = matcher.group(1);
                    var value = line.substring(matcher.end()).replaceAll("[{}()]", "");
                    switch (key) {
                        case "states" -> states = Stream.of(value.split(",")).map(String::strip).toList();
                        case "alphabet" -> alphabet = Stream.of(value.split(",")).map(String::strip).toList();
                        case "in_state" -> initialState = value;
                        case "out_states" -> outputStates = Stream.of(value.split(",")).map(String::strip).toList();
                        case "transitions" -> {
                            var transitionStrings = value.split(";");
                            transitions = new ArrayList<>();
                            for (var transitionString : transitionStrings) {
                                var transitionParts = transitionString.split(",");
                                transitions.add(new Transition(transitionParts[0].strip(), transitionParts[1].strip(), transitionParts[2].strip()));
                            }
                        }
                    }
                }
            });
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File not found");
        }
    }

    public boolean checkDeterministic() {
        var choices = new HashMap<String, HashMap<String, Set<String>>>();
        states.forEach(state -> choices.put(state, new HashMap<>()));
        alphabet.forEach(letter -> states.forEach(state -> choices.get(state).put(letter, new HashSet<>())));
        transitions.forEach(transition -> choices.get(transition.from()).get(transition.label()).add(transition.to()));
        return choices.entrySet().
                stream().
                allMatch(entry -> entry.getValue().values().stream().allMatch(entry1 -> entry1.size() <= 1));
    }

    private List<String> stringToList(String string) {
        return List.of(string.split(""));
    }

    private String listToString(List<String> list) {
        return String.join("", list);
    }

    private void printListOfString(String listName, List<String> list) {
        System.out.print(listName + "={");
        IntStream.range(0, list.size()).forEach(i -> {
            System.out.print(list.get(i));
            if (i != list.size() - 1) System.out.print(",");
        });
        System.out.println("}");
    }

    public void printStates() {
        printListOfString("states", states);
    }

    public void printAlphabet() {
        printListOfString("alphabet", alphabet);
    }

    public void printInitialState() {
        System.out.println("initialState=" + initialState);
    }

    public void printOutputStates() {
        printListOfString("outputStates", outputStates);
    }

    public void printTransitions() {
        System.out.println("transitions={");
        transitions.forEach(System.out::println);
        System.out.println("}");
    }

    public boolean checkAccepted(List<String> word) {
        var currentState = initialState;
        for (var letter : word) {
            var found = false;
            for (var transition : transitions) {
                if (transition.from().equals(currentState) && transition.label().equals(letter)) {
                    currentState = transition.to();
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        for (var outputState : outputStates) {
            if (outputState.equals(currentState)) return true;
        }
        return false;
    }

    public boolean checkAccepted(String word) {
        return checkAccepted(stringToList(word));
    }

    public List<String> getSubstringAccepted(List<String> word) {
        var currentState = initialState;
        var nextAccepted = new ArrayList<String>();
        for (var letter : word) {
            var found = false;
            for (var transition : transitions) {
                if (transition.from().equals(currentState) && transition.label().equals(letter)) {
                    currentState = transition.to();
                    found = true;
                    break;
                }
            }
            if (!found) break;
            nextAccepted.add(letter);
        }
        for (var outputState : outputStates) {
            if (outputState.equals(currentState)) return nextAccepted;
        }
        return null;
    }

    public String getSubstringAccepted(String word) {
        var substring = getSubstringAccepted(stringToList(word));
        return substring != null ? listToString(substring) : null;
    }
}
