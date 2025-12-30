package net.darkhax.spellbook.api.util;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDecimal128;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonNull;
import org.bson.BsonNumber;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.types.Decimal128;

import java.math.BigDecimal;

public class BsonHelper {

    public static BsonValue parseValue(String input) {
        input = input.trim();
        if (input.isEmpty() || input.equalsIgnoreCase("null")) {
            return BsonNull.VALUE;
        }
        final char first = input.charAt(0);
        if (first == '{') {
            return BsonDocument.parse(input);
        }
        else if (first == '[') {
            return BsonArray.parse(input);
        }
        else if (Character.isDigit(first) || first == '-' || first == '+') {
            return parseNumber(input);
        }
        else if (input.equalsIgnoreCase("true")) {
            return BsonBoolean.TRUE;
        }
        else if (input.equalsIgnoreCase("false")) {
            return BsonBoolean.FALSE;
        }
        return new BsonString(input);
    }

    public static BsonNumber parseNumber(String s) {
        try {
            if (s.contains(".") || s.contains("e") || s.contains("E")) {
                return new BsonDouble(Double.parseDouble(s));
            }
            final long l = Long.parseLong(s);
            if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                return new BsonInt32((int) l);
            }
            return new BsonInt64(l);

        }
        catch (NumberFormatException e) {
            return new BsonDecimal128(new Decimal128(new BigDecimal(s)));
        }
    }
}
