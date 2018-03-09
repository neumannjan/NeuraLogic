package constructs.factories;

import ida.ilp.logic.Constant;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * To keep track of created constants in some local scope (for constants sharing)
 * Created by gusta on 5.3.18.
 */
public class ConstantFactory {
    private static final Logger LOG = Logger.getLogger(ConstantFactory.class.getName());

    private Map<String, Constant> str2const;
    private Map<Constant, Constant> const2const;

    public ConstantFactory() {
        str2const = new HashMap<>();
        const2const = new HashMap<>();
    }

    public ConstantFactory(Collection<Constant> vars) {
        str2const = vars.stream().collect(Collectors.toMap(Constant::toString, Function.identity()));
        const2const = vars.stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
    }

    public Constant construct(String from) {
        Constant result = str2const.get(from);
        if (result == null) {
            result = Constant.construct(from);
            str2const.put(from, result);
            const2const.put(result, result);
        }
        return result;
    }

    public Constant construct(Constant from) {
        Constant result = const2const.get(from);
        if (result == null){
            str2const.put(result.toString(), result);
            const2const.put(result, result);
        }
        return from;
    }
}
