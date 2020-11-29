import utils.Calendar;
import utils.SLF4J;
import utils.XMLParser;

import javax.xml.stream.XMLStreamConstants;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PPS {
    private final int fulltime = 8;
    private static Random randomizer = new Random(06112020);

    private String name;                // the name of the planning system refers to its xml source file
    private int planningYear;                   // the year indicates the period of start and end dates of the projects
    private Set<Employee> employees;
    private Set<Project> projects;

    @Override
    public String toString() {
        return String.format("PPS_e%d_p%d", this.employees.size(), this.projects.size());
    }

    private PPS() {
        name = "none";
        planningYear = 2000;
        projects = new TreeSet<>();
        employees = new TreeSet<>();
    }
    private PPS(String resourceName, int year) {
        this();
        name = resourceName;
        planningYear = year;
    }

    /**
     * Reports the statistics of the project planning year
     */
    public void printPlanningStatistics() {
        System.out.printf("\nProject Statistics of '%s' in the year %d\n", name, planningYear);
        if (employees == null || projects == null || employees.size() == 0 || projects.size() == 0) {
            System.out.println("No employees or projects have been set up...");
            return;
        }

        System.out.printf("%d employees have been assigned to %d projects:\n\n",
                employees.size(), projects.size());
        System.out.printf("\n1. The average hourly wage of all employees is %.2f%%" , this.calculateAverageHourlyWage());
        System.out.printf("\n2. The longest project is \'%s\' with %d available workingdays",this.calculateLongestProject(),this.calculateLongestProject().getNumWorkingDays());
        System.out.printf("\n3. The follow employees have the broadest assignment in no less then %d different projects: %s",this.mostProjects(),this.calculateMostInvolvedEmployees().toString());
        System.out.printf("\n4. The total budget of committed project manpower is %d",this.calculateTotalManpowerBudget());
        System.out.printf("\n5. Below is an overview of the total managed budget by junior employees (hourly wage <= 26): %s","TODO");
        System.out.printf("\n6. Below is an overview of employees working at least 8 hours per day: %s",getFulltimeEmployees().toString());
        System.out.printf("\n7. Below is a overview of cumulative monthly project spends: %s\n","TODO");



    }

    /**
     * calculates the average hourly wage of all known employees in this system
     * @return
     */
    public double calculateAverageHourlyWage() {

        return  this.employees.stream().mapToDouble(Employee::getHourlyWage).sum()/this.employees.size() ;

    }

    /**
     * finds the project with the highest number of available working days.
     * (if more than one project with the highest number is found, any one is returned)
     * @return
     */
    public Project calculateLongestProject() {
        if(this.projects.size() != 0) {
            Project project = this.projects.stream().reduce((p1, p2) -> (p1.getNumWorkingDays() > p2.getNumWorkingDays() ? p1 : p2)).get();
            return project;
        }return null;
    }

    /**
     * calculates the total budget for assigned employees across all projects and employees in the system
     * based on the registration of committed hours per day per employee,
     * the number of working days in each project
     * and the hourly rate of each employee
     * @return
     */
    public int calculateTotalManpowerBudget() {
        // TODO
//        Map<Employee, Integer> test = new TreeMap<>() ;
//        for (Employee employee : this.employees){
//          Integer iets = employee.getAssignedProjects().stream().mapToInt(Project::calculateManpowerBudget).sum();
//          test.merge(employee,iets,Integer::sum);
//          test.putIfAbsent(employee,iets);
//        }
//        int sum = test.values().stream().reduce(0,Integer::sum);
//        return sum;
      return this.calculateManagedBudgetOverview(Employee::hasProjects).values().stream().reduce(0,Integer::sum);
//        return this.projects.stream().mapToInt(Project::calculateManpowerBudget).sum();
    }


    private int mostProjects(){
        return this.employees.stream().reduce((e1,e2) ->(e1.getAssignedProjects().size() > e2.getAssignedProjects().size() ? e1:e2)).get().getAssignedProjects().size();
    }

    /**
     * finds the employees that are assigned to the highest number of different projects
     * (if multiple employees are assigned to the same highest number of projects,
     * all these employees are returned in the set)
     * @return
     */
    public Set<Employee> calculateMostInvolvedEmployees() {


        Set <Employee> result = employees.stream().filter(employee -> employee.getAssignedProjects().size()== mostProjects()).collect(Collectors.toSet());
        return result;
    }

    /**
     * Calculates an overview of total managed budget per employee that complies with the filter predicate
     * The total managed budget of an employee is the sum of all man power budgets of all projects
     * that are being managed by this employee
     * @param filter
     * @return
     */
    public Map<Employee,Integer> calculateManagedBudgetOverview(Predicate<Employee> filter) {
        Map<Employee,Integer>result = new TreeMap<>();
        for (Employee employee :this.employees){
            if (filter.test(employee)){
                result.put(employee,employee.calculateManagedBudget());
            }
        }
        return result;
    }

    /**
     * Calculates and overview of total monthly spends across all projects in the system
     * The monthly spend of a single project is the accumulated manpower cost of all employees assigned to the
     * project across all working days in the month.
     * @return
     */
    public Map<Month,Integer> calculateCumulativeMonthlySpends() {
      this.projects.stream().filter(a -> a.getWorkingDays().)

        return null;

    }



    /**
     * Returns a set containing all the employees that work at least fulltime for at least one day per week on a project.
     * @return
     */
    public Set<Employee> getFulltimeEmployees() {

        Set<Employee> set = new TreeSet<>();
        for (Project project: projects){
            for (Map.Entry<Employee, Integer> entry : project.getCommittedHoursPerDay().entrySet()) {
                Employee k = entry.getKey();
                Integer v = entry.getValue();
                if (v >= fulltime){
                    set.add(k);

                }
            }
        }
        return set;
    }

    public String getName() {
        return name;
    }

    /**
     * A builder helper class to compose a small PPS using method-chaining of builder methods
     */
    public static class Builder {
        PPS pps;

        public Builder() {
            pps = new PPS();
        }

        private Employee getEmployee(int code){
            for (Employee a : this.pps.employees){
                if (a.getNumber() ==(code)){
                    return a;
                }
            }
            return null;
        }

        /**
         * Add another employee to the PPS being build
         * @param employee
         * @return
         */
        public Builder addEmployee(Employee employee) {
          this.pps.employees.add(employee);
            return this;
        }

        /**
         * Add another project to the PPS
         * register the specified manager as the manager of the new
         * @param project
         * @param manager
         * @return
         */
        public Builder addProject(Project project, Employee manager) {
            this.pps.projects.add(project);
            manager.getManagedProjects().add(project);
            manager.getAssignedProjects().add(project);
            return this;
        }

        /**
         * Add a commitment to work hoursPerDay on the project that is identified by projectCode
         * for the employee who is identified by employeeNr
         * This commitment is added to any other commitment that the same employee already
         * has got registered on the same project,
         * @param projectCode
         * @param employeeNr
         * @param hoursPerDay
         * @return
         */
        public Builder addCommitment(String projectCode, int employeeNr, int hoursPerDay) {
            this.pps.projects.forEach(a->{
                        if(a.getCode().equals(projectCode)){
                            a.addCommitment(getEmployee(employeeNr),hoursPerDay);
                        }
                    }
            );
            return this;
        }

        /**
         * Complete the PPS being build
         *
         * @return
         */
        public PPS build() {
            return pps;
        }
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    /**
     * Loads a complete configuration from an XML file
     *
     * @param resourceName the XML file name to be found in the resources folder
     * @return
     */
    public static PPS importFromXML(String resourceName) {
        XMLParser xmlParser = new XMLParser(resourceName);

        try {
            xmlParser.nextTag();
            xmlParser.require(XMLStreamConstants.START_ELEMENT, null, "projectPlanning");
            int year = xmlParser.getIntegerAttributeValue(null, "year", 2000);
            xmlParser.nextTag();

            PPS pps = new PPS(resourceName, year);

            Project.importProjectsFromXML(xmlParser, pps.projects);
            Employee.importEmployeesFromXML(xmlParser, pps.employees, pps.projects);

            return pps;

        } catch (Exception ex) {
            SLF4J.logException("XML error in '" + resourceName + "'", ex);
        }

        return null;
    }
}
