package b_streams.exercise;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.jupiter.api.Test;

import b_streams.data.Employee;
import b_streams.data.JobHistoryEntry;
import b_streams.data.Person;
import sun.text.resources.et.CollationData_et;

public class StreamsExercise {

    @Test
    public void getAllJobHistoryEntries() {
        final List<Employee> employees = getEmployees();

        final List<JobHistoryEntry> jobHistoryEntries = employees.stream()
                .flatMap(e -> e.getJobHistory().stream())
                .collect(Collectors.toList()); // TODO

        assertEquals(22, jobHistoryEntries.size());
    }

    @Test
    public void getSumDuration() {
        // sum all durations for all persons
        final List<Employee> employees = getEmployees();

        final int sumDurations = employees.stream()
                .flatMap(e -> e.getJobHistory().stream())
                .mapToInt(h -> h.getDuration())
                .sum(); // TODO

        assertEquals(72, sumDurations);
    }

    private static class PersonEmployer{
        private final Person person;
        private final String employer;

        public PersonEmployer(Person person, String employer) {
            this.person = person;
            this.employer = employer;
        }

        public Person getPerson() {
            return person;
        }

        public String getEmployer() {
            return employer;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("person", person)
                    .append("employer", employer)
                    .toString();
        }
    }

    @Test
    public void indexPersonsByEmployer1() {
        final List<Employee> employees = getEmployees();

        final Map<String, List<PersonEmployer>> index = employees.stream()
                .flatMap(e -> e.getJobHistory().stream()
                        .map(j -> new PersonEmployer(e.getPerson(), j.getEmployer())))
                .collect(Collectors.groupingBy(PersonEmployer::getEmployer)); // TODO

        assertEquals(11, index.get("epam").size());
    }

    @Test
    public void indexPersonsByEmployer2() {
        final List<Employee> employees = getEmployees();

        final Map<String, List<Person>> index = employees.stream()
                .flatMap(e -> e.getJobHistory().stream()
                        .map(j -> new PersonEmployer(e.getPerson(), j.getEmployer())))
                .collect(Collectors.groupingBy(PersonEmployer::getEmployer,
                        Collectors.mapping(PersonEmployer::getPerson, Collectors.toList())));; // TODO

        assertEquals(11, index.get("epam").size());
    }

    private static class PersonDuration {
        private final Person person;
        private final int duration;

        public PersonDuration(Person person, int duration) {
            this.person = person;
            this.duration = duration;
        }

        public Person getPerson() {
            return person;
        }

        public int getDuration() {
            return duration;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("person", person)
                    .append("duration", duration)
                    .toString();
        }
    }

    private PersonDuration sumAllPersonDurations(Employee e) {
        // TODO
        return new PersonDuration(e.getPerson(), e.getJobHistory().stream()
                .mapToInt(JobHistoryEntry::getDuration).sum());
        //throw new UnsupportedOperationException();
    }

    @Test
    public void getSumPersonDuration() {
        // sum all durations for each person
        final List<Employee> employees = getEmployees();

        final Map<Person, Integer> personDuration = employees.stream()
                .collect(
                        HashMap::new,
                        (map, e) -> map.put(e.getPerson(), sumAllPersonDurations(e).getDuration()),
                        HashMap::putAll
                ); // TODO use sumAllPersonDurations

        assertEquals(Integer.valueOf(8), personDuration.get(new Person("John", "Doe", 24)));
    }

    private static class PersonPositionIndex {
        private final Person person;
        private final Map<String, Integer> durationByPositionIndex;

        public PersonPositionIndex(Person person, Map<String, Integer> durationByPositionIndex) {
            this.person = person;
            this.durationByPositionIndex = durationByPositionIndex;
        }

        public Person getPerson() {
            return person;
        }

        public Map<String, Integer> getDurationByPositionIndex() {
            return durationByPositionIndex;
        }
    }

    private static PersonPositionIndex getPersonPositionIndex(Employee e) {
        final AtomicInteger counter = new AtomicInteger();
        return new PersonPositionIndex(e.getPerson(), e.getJobHistory().stream()
                .collect(
                        HashMap::new,
                        (map, j) -> map.put(j.getPosition(), counter.getAndIncrement()),
                        HashMap::putAll
                        )
                );
//        throw new UnsupportedOperationException();
    }

    @Test
    public void getSumDurationsForPersonByPosition() {
        final List<Employee> employees = getEmployees();

        final List<PersonPositionIndex> personIndexes = employees.stream()
                .map(e -> new PersonPositionIndex(e.getPerson(),
                        getPersonPositionIndex(e).getDurationByPositionIndex()))
                .collect(Collectors.toList()); // TODO use getPersonPositionIndex

        assertEquals(1, personIndexes.get(3).getDurationByPositionIndex().size());
    }

    private static class PersonPositionDuration {
        private final Person person;
        private final String position;
        private final int duration;

        public PersonPositionDuration(Person person, String position, int duration) {
            this.person = person;
            this.position = position;
            this.duration = duration;
        }

        public Person getPerson() {
            return person;
        }

        public String getPosition() {
            return position;
        }

        public int getDuration() {
            return duration;
        }
    }

    @Test
    public void getDurationsForEachPersonByPosition() {
        final List<Employee> employees = getEmployees();

        final List<PersonPositionDuration> personPositionDurations =  employees.stream()
                .filter(e -> !e.getJobHistory().isEmpty())
                .map(e -> {
                    final HashMap<String, Integer> positionDuration = new HashMap<>();
                    e.getJobHistory().forEach(j -> {
                        if (positionDuration.containsKey(j.getPosition())) {
                            positionDuration.put(j.getPosition(), positionDuration.get(j.getPosition()) + j.getDuration());
                        } else {
                            positionDuration.put(j.getPosition(), j.getDuration());
                        }
                    });
                    return positionDuration.entrySet().stream()
                            .map(entry -> new PersonPositionDuration(e.getPerson(), entry.getKey(), entry.getValue()));
                }).collect(
                        ArrayList::new,
                        (list, p) -> list.addAll(p.collect(Collectors.toList())),
                        ArrayList::addAll
                );
                // TODO


        assertEquals(17, personPositionDurations.size());
    }

    @Test
    public void getCoolestPersonByPosition1() {
        // Get person with max duration on given position
        final List<Employee> employees = getEmployees();

        final Map<String, PersonPositionDuration> coolestPersonByPosition = employees.stream()
                .flatMap(e -> e.getJobHistory().stream()
                        .map(j -> new PersonPositionDuration(e.getPerson(), j.getPosition(), j.getDuration())))
                .sorted(Comparator.comparingInt(PersonPositionDuration::getDuration)
                        .thenComparing((p1, p2) -> p2.getPerson().getAge() - p1.getPerson().getAge()))
                .collect(
                        HashMap::new,
                        (map, p) -> map.put(p.getPosition(), p),
                        HashMap::putAll
                );
                // TODO


        assertEquals(new Person("John", "White", 22), coolestPersonByPosition.get("QA").getPerson());
    }

    @Test
    public void getCoolestPersonByPosition2() {
        // Get person with max duration on given position
        final List<Employee> employees = getEmployees();

        final Map<String, Person> coolestPersonByPosition = employees.stream()
                .flatMap(e -> e.getJobHistory().stream()
                        .map(j -> new PersonPositionDuration(e.getPerson(), j.getPosition(), j.getDuration())))
                .sorted(Comparator.comparingInt(PersonPositionDuration::getDuration)
                        .thenComparing((p1, p2) -> p2.getPerson().getAge() - p1.getPerson().getAge()))
                .collect(
                        HashMap::new,
                        (map, p) -> map.put(p.getPosition(), p.getPerson()),
                        HashMap::putAll
                ); // TODO


        assertEquals(new Person("John", "White", 22), coolestPersonByPosition.get("QA"));
    }

    private List<Employee> getEmployees() {
        return Arrays.asList(
                new Employee(
                        new Person("John", "Galt", 20),
                        Collections.emptyList()),
                new Employee(
                        new Person("John", "Doe", 21),
                        Arrays.asList(
                                new JobHistoryEntry(4, "BA", "yandex"),
                                new JobHistoryEntry(2, "QA", "epam"),
                                new JobHistoryEntry(2, "dev", "abc")
                        )),
                new Employee(
                        new Person("John", "White", 22),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "QA", "epam")
                        )),
                new Employee(
                        new Person("John", "Galt", 23),
                        Arrays.asList(
                                new JobHistoryEntry(3, "dev", "epam"),
                                new JobHistoryEntry(2, "dev", "google")
                        )),
                new Employee(
                        new Person("John", "Doe", 24),
                        Arrays.asList(
                                new JobHistoryEntry(4, "QA", "yandex"),
                                new JobHistoryEntry(2, "BA", "epam"),
                                new JobHistoryEntry(2, "dev", "abc")
                        )),
                new Employee(
                        new Person("John", "White", 25),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "QA", "epam")
                        )),
                new Employee(
                        new Person("John", "Galt", 26),
                        Arrays.asList(
                                new JobHistoryEntry(3, "dev", "epam"),
                                new JobHistoryEntry(1, "dev", "google")
                        )),
                new Employee(
                        new Person("Bob", "Doe", 27),
                        Arrays.asList(
                                new JobHistoryEntry(4, "QA", "yandex"),
                                new JobHistoryEntry(2, "QA", "epam"),
                                new JobHistoryEntry(2, "dev", "abc")
                        )),
                new Employee(
                        new Person("John", "White", 28),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "BA", "epam")
                        )),
                new Employee(
                        new Person("John", "Galt", 29),
                        Arrays.asList(
                                new JobHistoryEntry(3, "dev", "epam"),
                                new JobHistoryEntry(1, "dev", "google")
                        )),
                new Employee(
                        new Person("John", "Doe", 30),
                        Arrays.asList(
                                new JobHistoryEntry(4, "QA", "yandex"),
                                new JobHistoryEntry(2, "QA", "epam"),
                                new JobHistoryEntry(5, "dev", "abc")
                        )),
                new Employee(
                        new Person("Bob", "White", 31),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "QA", "epam")
                        ))
        );
    }


}