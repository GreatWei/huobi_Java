package com.huobi.my;

import com.huobi.model.market.Candlestick;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DataFactor {
    double _cmp_min(double a, double b) {
        return a < b ? a : b;
    }

    double _cmp_max(double a, double b) {
        return a > b ? a : b;
    }

    double _cmp_max(double a, double b, double c) {
        double d = a > b ? a : b;
        return d > c ? d : c;
    }

    List<BigDecimal> _ticks(List<Candlestick> candlestickList){
        if(candlestickList==null||candlestickList.size()==0)
            return new ArrayList<BigDecimal>();
        List<BigDecimal> doubleList = new ArrayList<BigDecimal>(50);
        for (Candlestick candlestick:candlestickList)
            doubleList.add(candlestick.getClose());
        return doubleList;

    }

    Object MACD(List<Candlestick> records, int fastEMA , int slowEMA , int signalEMA ) {
        List<BigDecimal> ticks =_ticks(records);
        List<BigDecimal> dif = _diff(_ema(ticks, fastEMA), _ema(ticks, slowEMA));
        List<Double> signal = _ema(dif, signalEMA);
        List<Double> histogram = _diff(dif, signal);
        return {{dif, signal, histogram}};
    }

    List<List<double>> KDJ(Records &records, int n =9, int k =3, int d =3) {
        int length = records.size();
        List<double> RSV (length, 0);
        _set(RSV, 0, n - 1, NAN);
        List<double> K (length, 0);
        List<double> D (length, 0);
        List<double> J (length, 0);

        List<double> hs = records.High();
        List<double> ls = records.Low();

        for (int i = 0; i < length; i++) {
            if (i >= int(n - 1)) {
                double c = records[i].Close;
                double h = _cmp(hs, i - (n - 1), i + 1, _cmp_max);
                double l = _cmp(ls, i - (n - 1), i + 1, _cmp_min);
                RSV[i] = h != l ? (100 * ((c - l) / (h - l))) : 100;
                K[i] = (1 * RSV[i] + (k - 1) * K[i - 1]) / k;
                D[i] = (1 * K[i] + (d - 1) * D[i - 1]) / d;
            } else {
                K[i] = D[i] = 50;
                RSV[i] = 0;
            }
            J[i] = 3 * K[i] - 2 * D[i];
        }
        for (int i = 0; i < n - 1; i++) {
            K[i] = D[i] = J[i] = NAN;
        }
        return {{K, D, J}};
    }

    List<double> RSI(Records &records, int period =14) {
        int i = 0;
        int n = period;
        List<double> rsi (records.size(), 0);
        _set(rsi, 0, rsi.size(), NAN);
        if (records.size() < n) {
            return rsi;
        }
        List<double> ticks = records.Close();
        List<double> deltas = _move_diff(ticks);
        List<double> seed (deltas.begin(), deltas.begin() + n);
        double up = 0.0;
        double down = 0.0;
        for (i = 0; i < seed.size(); i++) {
            if (seed[i] >= 0) {
                up += seed[i];
            } else {
                down += seed[i];
            }
        }
        up /= n;
        down /= n;
        down = -down;
        double rs = down != 0 ? up / down : 0;
        rsi[n] = 100 - 100 / (1 + rs);
        double delta = 0.0;
        double upval = 0.0;
        double downval = 0.0;
        for (i = n + 1; i < ticks.size(); i++) {
            delta = deltas[i - 1];
            if (delta > 0) {
                upval = delta;
                downval = 0;
            } else {
                upval = 0;
                downval = -delta;
            }
            up = (up * (n - 1) + upval) / n;
            down = (down * (n - 1) + downval) / n;
            rs = up / down;
            rsi[i] = 100 - 100 / (1 + rs);
        }
        return rsi;
    }

    List<double> ATR(Records &records, int period =14) {
        List<double> ret;
        if (records.size() == 0) {
            return ret;
        }
        List<double> R (records.size(), 0);
        double sum = 0.0;
        double n = 0.0;
        for (int i = 0; i < records.size(); i++) {
            double TR = 0.0;
            if (i == 0) {
                TR = records[i].High - records[i].Low;
            } else {
                TR = _cmp_max(records[i].High - records[i].Low, abs(records[i].High - records[i - 1].Close), abs(records[i - 1].Close - records[i].Low));
            }
            sum += TR;
            if (i < period) {
                n = sum / (i + 1);
            } else {
                n = (((period - 1) * n) + TR) / period;
            }
            R[i] = n;
        }
        return R;
    }

    List<double> OBV(Records &records) {
        List<double> R;
        if (records.size() == 0) {
            return R;
        }
        for (int i = 0; i < records.size(); i++) {
            if (i == 0) {
                R.push_back(records[i].Volume);
            } else if (records[i].Close >= records[i - 1].Close) {
                R.push_back(R[i - 1] + records[i].Volume);
            } else {
                R.push_back(R[i - 1] - records[i].Volume);
            }
        }
        return R;
    }

    List<double> MA(Records &records, int period =9) {
        return _sma(records.Close(), period);
    }

    List<double> EMA(Records &records, int period =9) {
        return _ema(records.Close(), period);
    }

    List<List<double>, 3>

    BOLL(Records &records, int period =20, double multiplier =2) {
        List<double> S = records.Close();
        int j = 0;
        for (j = period - 1; j < S.size() && isnan(S[j]); j++) ;
        List<double> UP (S.size(), 0);
        List<double> MB (S.size(), 0);
        List<double> DN (S.size(), 0);
        _set(UP, 0, j, NAN);
        _set(MB, 0, j, NAN);
        _set(DN, 0, j, NAN);
        double sum = 0;
        for (int i = j; i < S.size(); i++) {
            if (i == j) {
                for (int k = 0; k < period; k++) {
                    sum += S[k];
                }
            } else {
                sum = sum + S[i] - S[i - period];
            }
            double ma = sum / period;
            double d = 0.0;
            for (int k = i + 1 - period; k <= i; k++) {
                d += (S[k] - ma) * (S[k] - ma);
            }
            double stdev = sqrt(d / period);
            double up = ma + (multiplier * stdev);
            double dn = ma - (multiplier * stdev);
            UP[i] = up;
            MB[i] = ma;
            DN[i] = dn;
        }
        return {{UP, MB, DN}};
    }

    List<List<double>, 3>

    Alligator(Records &records, int jawLength =13, int teethLength =8, int lipsLength =5) {
        List<double> ticks;
        for (int i = 0; i < records.size(); i++) {
            ticks.push_back((records[i].High + records[i].Low) / 2);
        }
        List<double> jaw = _smma(ticks, jawLength);
        jaw.insert(jaw.begin(), 8, NAN);
        List<double> teeth = _smma(ticks, teethLength);
        teeth.insert(teeth.begin(), 5, NAN);
        List<double> lips = _smma(ticks, lipsLength);
        lips.insert(lips.begin(), 3, NAN);
        return {{jaw, teeth, lips}};
    }

    List<double> CMF(Records &records, int periods =20) {
        List<double> ret;
        double sumD = 0.0;
        double sumV = 0.0;
        List<double> arrD;
        List<double> arrV;
        for (int i = 0; i < records.size(); i++) {
            double d = (records[i].High == records[i].Low) ? 0 : (2 * records[i].Close - records[i].Low - records[i].High) / (records[i].High - records[i].Low) * records[i].Volume;
            arrD.push_back(d);
            arrV.push_back(records[i].Volume);
            sumD += d;
            sumV += records[i].Volume;
            if (i >= periods) {
                sumD -= arrD.front();
                arrD.erase(arrD.begin());
                sumV -= arrV.front();
                arrV.erase(arrV.begin());
            }
            ret.push_back(sumD / sumV);
        }
        return ret;
    }

    double Highest(List<double> records, int n) {
        return _filt(records, n, NAN, _cmp_max);
    }

    double Lowest(List<double> records, int n) {
        return _filt(records, n, NAN, _cmp_min);
    }

    double _filt(List<double> records, double n, double iv, double(*pfun) (
    double a, double b))

    {
        if (records.size() < 2) {
            return NAN;
        }
        double v = iv;
        double pos = n != 0 ? records.size() - _cmp_min(records.size() - 1, n) - 1 : 0;
        for (int i = records.size() - 2; i >= pos; i--) {
            v = pfun(v, records[i]);
        }
        return v;
    }

    List<double> _smma(List<double> S, int period) {
        int length = S.size();
        List<double> R (length, 0);
        int j = _skip(S, period);
        _set(R, 0, j, NAN);
        if (j < length) {
            R[j] = _avg(S, j + 1);
            for (int i = j + 1; i < length; i++) {
                R[i] = (R[i - 1] * (period - 1) + S[i]) / period;
            }
        }
        return R;
    }

    List<double> _move_diff(List<double> a) {
        List<double> d;
        for (int i = 1; i < a.size(); i++) {
            d.push_back(a[i] - a[i - 1]);
        }
        return d;
    }

    List<BigDecimal> _ema(List<BigDecimal> S, int period) {
        int length = S.size();
        List<BigDecimal> R = new ArrayList<BigDecimal>(length);
        BigDecimal multiplier = new BigDecimal("2.0").divide(new BigDecimal(period + 1));
        int j = _skip(S, period);
        _set(R, 0, j, Double.NaN);
        if (j < length) {
            R[j] = _avg(S, j + 1);
            for (int i = j + 1; i < length; i++) {
                R[i] = (S[i] - R[i - 1]) * multiplier + R[i - 1];
            }
        }
        return R;
    }

    List<double> _sma(List<double> S, int period) {
        List<double> R (S.size(), 0);
        int j = _skip(S, period);
        _set(R, 0, j, NAN);
        if (j < S.size()) {
            double sum = 0;
            for (int i = j; i < S.size(); i++) {
                if (i == j) {
                    sum = _sum(S, i + 1);
                } else {
                    if (i < period) {
                        R[i] = NAN;
                        continue;
                    }
                    sum += S[i] - S[i - period];
                }
                R[i] = sum / period;
            }
        }
        return R;
    }

    double _sum(List<Double> arr, int num) {
        double sum = 0.0;
        for (int i = 0; i < num; i++) {
                sum += arr[i];
        }
        return sum;
    }

    List<BigDecimal> _diff(List<BigDecimal> a, List<BigDecimal> b) {
        List<Double> d = new ArrayList<Double>();
        for (int i = 0; i < b.size(); i++) {
                double a=a[i].;
                d.add(a[i] - b[i]);
        }
        return d;
    }

    BigDecimal _avg(List<BigDecimal> arr, double num) {
        int n = 0;
        double sum = 0.0;
        for (int i = 0; i < num; i++) {
            if (!isnan(arr[i])) {
                sum += arr[i];
                n++;
            }
        }
        return sum / n;
    }

    void _set(List<BigDecimal> arr, int start, int end, double value) {
        int e = arr.size()<end?arr.size(): end;
        for (int i = start; i < e; i++) {
          arr.set(i,new BigDecimal(value));
        }
    }

    int _skip(List<BigDecimal> arr, int period) {
        int j = 0;
        for (int k = 0; j < arr.size(); j++) {
                k++;
            if (k == period) {
                break;
            }
        }
        return j;
    }

    double _cmp(List<double> arr, int start, int end, double(*pfun) (
    double a, double b))

    {
        double v = arr[start];
        for (int i = start; i < end; i++) {
            v = pfun(arr[i], v);
        }
        return v;
    }
};
}
