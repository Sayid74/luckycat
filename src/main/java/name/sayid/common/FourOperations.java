package name.sayid.common;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * This class implements four operations calculating it parses from
 * a expression string. You can call parser and give a expression string
 * it will return a string represents the expression result. The class
 * even gives a static method eval, this method can provides a simple
 * using.
 */
public class FourOperations {
    private final LinkedList<Long>      _values  = new LinkedList();
    private final LinkedList<Character> _handles = new LinkedList();

    private final static  int NO_VALUE = 0;
    private final static  int NUMERIC  = 1;
    private final static  int OPERATOR = 2;

    private final static Set<Character> _NUM_CHARS
        = Set.of('1','2','3','4','5','6','7','8','9','0');
    private final static Set<Character> LEVEL_0 = Set.of('+', '-');
    private final static Set<Character> LEVEL_1 = Set.of('*', '/');
    private final static Set<Character> ALL_OPTS
        = new HashSet<>(LEVEL_0){{ addAll(LEVEL_1); }};

    private String _input;

    private int _pos    = 0;
    private int _status = NO_VALUE;

    Function<Character, Set<Character>> upLeveSetOf = (c) -> {
        if (LEVEL_0.contains(c))
            return new HashSet(LEVEL_0) {{ addAll(LEVEL_1); }};
        else if (LEVEL_1.contains(c))
            return new HashSet<>(LEVEL_1);
        else return Set.of();
    };

    BooleanSupplier unfinished = () -> _pos < _input.length();
    BooleanSupplier isNumber   = () -> _NUM_CHARS.contains(_input.charAt(_pos));

    LongSupplier addOpt      = () -> _values.pop() + _values.pop();
    LongSupplier minusOpt    = () -> _values.remove(1) - _values.pop();
    LongSupplier multiplyOpt = () -> _values.pop() * _values.pop();
    LongSupplier divisionOpt = () -> _values.remove(1) / _values.pop();

    Map<Character, LongSupplier> selector = Map.of (
        '+', addOpt
        , '-', minusOpt
        , '*' , multiplyOpt
        , '/' , divisionOpt);

    /**
     * The FourOperations instance through this method provides
     * parser and calculating.
     * @param input, It is a four operations expression.
     * @return The result of expression calculated.
     * @throws Exception
     */
    public String parser(String input) throws FourOperationsException
    {
        _input = input;
        if (_input.isBlank() ) return null;
        do {
            if (_input.charAt(_pos) == '(') doLeftQuoter();
            else if (_input.charAt(_pos)==')') doRightQuoter();
            else if (isNumber.getAsBoolean()) doNumber();
            else if (ALL_OPTS.contains(_input.charAt(_pos))) calculate();
            else throw new FourOperationsException("Unexpected expression.");
            _pos++;
        } while(_pos < _input.length());

        calculateAll();
        if (_values.size() == 1 && _status == NUMERIC && _handles.size() == 0)
            return _values.pop().toString();
        else throw new FourOperationsException("The express is illegual.");
    }

    private void calculate() throws FourOperationsException
    {
        if (_status != NUMERIC)
            throw new FourOperationsException("It hopes a character in 0~9.");
        char c = _input.charAt(_pos);
        while (!_handles.isEmpty() && upLeveSetOf.apply(c).contains(_handles.getFirst()))
            _values.push(selector.get(_handles.pop()).getAsLong());

        _handles.push(c);
        _status = OPERATOR;
    }

    private void calculateAll() throws FourOperationsException {
       if (_status != NUMERIC)
           throw new FourOperationsException("There isn't a characer in 0~9.");
       while(!_handles.isEmpty() && _handles.getFirst() != '(') {
           _values.push(selector.get(_handles.pop()).getAsLong());
       }
       _status = NUMERIC;
    }

    private void doLeftQuoter() throws FourOperationsException{
        if (Set.of(OPERATOR, NO_VALUE).contains(_status)){
            _handles.push('(');
            _status = NO_VALUE;
        }
        else throw new FourOperationsException("Expression is unintended.");
    }

    private void doRightQuoter() throws FourOperationsException{
        if (NUMERIC == _status) {
            calculateAll();
            if (_handles.getFirst() != '(')
                throw new FourOperationsException(
                    "It requires a character '(' at here.");
            else
                _handles.pop();
        }
        else throw new FourOperationsException(
            "It requires a number or '(' at here.");
    }

    private void doNumber() throws FourOperationsException{
        if (Set.of(OPERATOR,  NO_VALUE).contains(_status)) {
            _values.push(whileNum());
            _status = NUMERIC;
        } else
            throw new FourOperationsException(
                "If not at beginning, there should be a operator.");
    }

    private long whileNum() {
        var sb = new StringBuilder();
        while(unfinished.getAsBoolean() && isNumber.getAsBoolean())
           sb.append(_input.charAt(_pos++));

        _pos--;
        return Long.parseLong(sb.toString());
    }

    /**
     * Calculate the input expression's result.
     * @param expression, It is four operations express.
     * @return the input expression result.
     * @throws FourOperationsException
     * any exception occured, it should be
     * throw out.
     */
    public static String eval(String expression) throws FourOperationsException{
        return (new FourOperations()).parser(expression);
    }

    public static class FourOperationsException extends Exception {
        public FourOperationsException() {
        }

        public FourOperationsException(String message) {
            super(message);
        }

        public FourOperationsException(String message, Throwable cause) {
            super(message, cause);
        }

        public FourOperationsException(Throwable cause) {
            super(cause);
        }

        public FourOperationsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
