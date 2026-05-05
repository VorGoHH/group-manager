package group_manager;

import group_manager.entity.DutyRole;
import group_manager.entity.Soldier;
import group_manager.repository.DutyRoleRepository;
import group_manager.repository.SoldierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SoldierRepository soldierRepository;
    private final DutyRoleRepository dutyRoleRepository;

    @Override
    public void run(String... args) {

        // Ролі — окрема перевірка, незалежно від солдатів
        if (dutyRoleRepository.count() == 0) {
            dutyRoleRepository.save(new DutyRole(null, "Черговий ПУ", true, 1));
            dutyRoleRepository.save(new DutyRole(null, "Помічник ЧПУ", false, 1));
            dutyRoleRepository.save(new DutyRole(null, "Днювальний", false, 2));
            dutyRoleRepository.save(new DutyRole(null, "Їдальня", false, 1));
        }

        if (soldierRepository.count() > 0) return;

        // Командування
        save("Вашуленко",     "Дмитро",    "Андрійович",    "молодший сержант", 1, true,  false);
        save("Каніболоцький", "Максим",    "Євгенович",     "сержант",          1, false, true);

        // 1 відділення
        save("Кіндратяк",    "Вадим",     "Вікторович",    "молодший сержант", 1, true,  false);
        save("Богаченко",    "Павло",     "Іванович",      "старший солдат",   1, false, false);
        save("Демченко",     "Дмитро",    "Романович",     "солдат",           1, false, false);
        save("Корчинський",  "Володимир", "В'ячеславович", "солдат",           1, false, false);
        save("Кравченко",    "Дмитро",    "Вікторович",    "солдат",           1, false, false);
        save("Марченко",     "Андрій",    "Миколайович",   "солдат",           1, false, false);
        save("Лобач",        "Віктор",    "Михайлович",    "солдат",           1, false, false);
        save("Сехін",        "Нікіта",    "Сергійович",    "солдат",           1, false, false);

        // 2 відділення
        save("Бондаренко",   "Андрій",    "Андрійович",    "солдат",           2, true,  false);
        save("Базелюк",      "Олександр", "Вадимович",     "солдат",           2, false, false);
        save("Вітвіцький",   "Олег",      "Вадимович",     "солдат",           2, false, false);
        save("Кульбако",     "Ростислав", "Сергійович",    "солдат",           2, false, false);
        save("Радківський",  "Владислав", "Миколайович",   "солдат",           2, false, false);
        save("Тимашов",      "Микита",    "Денисович",     "солдат",           2, false, false);
        save("Ходоровський", "Данило",    "Андрійович",    "солдат",           2, false, false);

        // 3 відділення
        save("Рязанов",      "Кирило",    "Олександрович", "солдат",           3, true,  false);
        save("Зубаков",      "Дмитро",    "Олександрович", "солдат",           3, false, false);
        save("Кінах",        "Владислав", "Олександрович", "солдат",           3, false, false);
        save("Милетич",      "Ростислав", "Олександрович", "солдат",           3, false, false);
        save("Мясоєдов",     "Захар",     "Арсенович",     "солдат",           3, false, false);
        save("Снігир",       "Артем",     "Миколайович",   "солдат",           3, false, false);
        save("Харченко",     "Ярослав",   "Павлович",      "солдат",           3, false, false);
        save("Купрієнко",    "Єгор",      "Максимович",    "молдший сержант",  3,true,false);

        System.out.println(">>> Дані узавантажені");
    }

    private void save(String lastName, String firstName, String middleName,
                      String rank, int platoon, boolean isCommander, boolean excludeFromDuty) {
        Soldier s = new Soldier();
        s.setLastName(lastName);
        s.setFirstName(firstName);
        s.setMiddleName(middleName);
        s.setRank(rank);
        s.setPlatoon(platoon);
        s.setIsCommander(isCommander);
        s.setIsActive(true);
        s.setExcludeFromDuty(excludeFromDuty);
        soldierRepository.save(s);
    }
}