package com.huobi.my;

import com.huobi.model.market.Candlestick;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DataFactor {
    double _cmp_min(double a, double b) {
        return a < b ? a : b;
    }

    int _cmp_min(int a, int b) {
        return a < b ? a : b;
    }


    double _cmp_max(double a, double b) {
        return a > b ? a : b;
    }

    double _cmp_max(double a, double b, double c) {
        double d = a > b ? a : b;
        return d > c ? d : c;
    }

    List<Double> _ticks(List<Candlestick> records) {
        if (records == null || records.size() == 0)
            return new ArrayList<Double>();
        List<Double> doubleList = new ArrayList<Double>();
        for (Candlestick candlestick : records)
            doubleList.add(candlestick.getClose().doubleValue());
        return doubleList;
    }

    List<Double> MACD(List<Candlestick> records, int fastEMA, int slowEMA, int signalEMA) {
        List<Double> ticks = _ticks(records);
        List<Double> dif = _diff(_ema(ticks, fastEMA), _ema(ticks, slowEMA));
        List<Double> signal = _ema(dif, signalEMA);
        List<Double> histogram = _diff(dif, signal);
        return {{dif, signal, histogram}};
    }

    array<List<double>, 3>

    KDJ(Records &records, int n =9, int k =3, int d =3) {
        int length = records.size();
        List<double> RSV (length, 0);
        _set(RSV, 0, n - 1, NAN);
        List<double> K (length, 0);
        List<double> D (length, 0);
        List<double> J (length, 0);

        List<double> hs = records.High();
        List<double> ls = records.Low();

        for (int i = 0; i < length; i++) {
            if (i >= int(n - 1)){
                double c = records[i].Close;
                double h = _cmp(hs, i - (n - 1), i + 1, _cmp_max);
                double l = _cmp(ls, i - (n - 1), i + 1, _cmp_min);
                RSV[i] = h != l ? (100 * ((c - l) / (h - l))) : 100;
                K[i] = (1 * RSV[i] + (k - 1) * K[i - 1]) / k;
                D[i] = (1 * K[i] + (d - 1) * D[i - 1]) / d;
            } else{
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


    List<List<Double>> BOLL(List<d> &records, int period =20, double multiplier =2) {
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


    List<List<Double>> Alligator(List<Candlestick> records, int jawLength , int teethLength , int lipsLength ) {
        List<Double> ticks = new ArrayList<Double>();
        for (int i = 0; i < records.size(); i++) {
            ticks.add((records.get(i).getHigh().doubleValue() + records.get(i).getLow().doubleValue()) / 2);
        }
        List<Double> jaw = _smma(ticks, jawLength);

        jaw = insertList(8,Double.NaN,jaw);
        List<Double> teeth = _smma(ticks, teethLength);
       // teeth.insert(teeth.begin(), 5, NAN);
        teeth=insertList(5,Double.NaN,teeth);
        List<Double> lips = _smma(ticks, lipsLength);
       // lips.insert(lips.begin(), 3, NAN);
       lips=insertList(3,Double.NaN,lips);
       List<List<Double>> result = new ArrayList<List<Double>>();
       result.add(jaw);// jaw (blue)
       result.add(teeth);//teeth (red)
       result.add(lips);//lips (green)
        return result;
    }

    public List<Double> insertList(int num,double value,List<Double> doubleList){
        List<Double> list = new ArrayList<Double>();
        for(int i=0;i<num;i++){
            list.add(value);
        }
        list.addAll(doubleList);
        return list;
    }

    List<Double> CMF(List<Candlestick> records, int periods) {
        List<Double> ret = new ArrayList<Double>();
        double sumD = 0.0;
        double sumV = 0.0;
        Queue<Double> arrD = new LinkedList<Double>();
        Queue<Double> arrV = new LinkedList<Double>();
        for (int i = 0; i < records.size(); i++) {
            double d = (records.get(i).getHigh().doubleValue() == records.get(i).getLow().doubleValue()) ? 0 : (2 * records.get(i).getClose().doubleValue() - records.get(i).getLow().doubleValue() - records.get(i).getHigh().doubleValue()) / (records.get(i).getHigh().doubleValue() - records.get(i).getLow().doubleValue()) * records.get(i).getVol().doubleValue();
            arrD.offer(d);
            arrV.offer(records.get(i).getVol().doubleValue());
            sumD += d;
            sumV += records.get(i).getVol().doubleValue();
            if (i >= periods) {
                sumD -= arrD.poll();
                sumV -= arrV.poll();

            }
            ret.add(sumD / sumV);
        }
        return ret;
    }

    double Highest(List<Double> records, int n) {
        return _filt(records, n, Double.NaN, (a,b)->{
            return a > b ? a : b;
        });
    }

    double Lowest(List<Double> records, int n) {
        return _filt(records, n, Double.NaN, (a,b)->{
            return a < b ? a : b;
        });
    }

    double _filt(List<Double> records, double n, double iv, Methods<Double> methods)

    {
        if (records.size() < 2) {
            return Double.NaN;
        }
        double v = iv;
        double pos = n != 0 ? records.size() - _cmp_min(records.size() - 1, n) - 1 : 0;
        for (int i = records.size() - 2; i >= pos; i--) {
            v = methods.methodsChose(v, records.get(i));
        }
        return v;
    }

    List<Double> _smma(List<Double> S, int period) {
        int length = S.size();
        List<Double> R = new ArrayList<Double> (length);
        int j = _skip(S, period);
        _set(R, 0, j, Double.NaN);
        if (j < length) {
            R.add(_avg(S, j + 1));
            for (int i = j + 1; i < length; i++) {
                R.add((R.get(i - 1) * (period - 1) + S.get(i)) / period);
            }
        }
        return R;
    }

    List<Double> _move_diff(List<Double> a) {
        List<Double> d=new ArrayList<Double>();
        for (int i = 1; i < a.size(); i++) {
            d.add(a.get(i) - a.get(i - 1));
        }
        return d;
    }

    List<Double> _ema(List<Double> S, int period) {
        int length = S.size();
        List<Double> R = new ArrayList<Double>(length);
        double multiplier = 2.0 / (period + 1);
        int j = _skip(S, period);
        _set(R, 0, j, Double.NaN);
        if (j < length) {
            R.add(_avg(S, j + 1));
            for (int i = j + 1; i < length; i++) {
                R.add(((S.get(i) - R.get(i - 1)) * multiplier + R.get(i - 1)));
            }
        }
        return R;
    }

    List<Double> _sma(List<Double> S, int period) {
        List<Double> R = new ArrayList<Double>(S.size());
        int j = _skip(S, period);
        _set(R, 0, j, Double.NaN);
        if (j < S.size()) {
            double sum = 0;
            for (int i = j; i < S.size(); i++) {
                if (i == j) {
                    sum = _sum(S, i + 1);
                } else {
                    if (i < period) {
                        R.set(i, Double.NaN);
                        continue;
                    }
                    sum += S.get(i) - S.get(i - period);
                }
                R.set(i, sum / period);
            }
        }
        return R;
    }

    double _sum(List<Double> arr, int num) {
        double sum = 0.0;
        for (int i = 0; i < num; i++) {
            if (!Double.isNaN(arr.get(i))) {
                sum += arr.get(i);
            }
        }
        return sum;
    }

    List<Double> _diff(List<Double> a, List<Double> b) {
        List<Double> d = new ArrayList<Double>();
        for (int i = 0; i < b.size(); i++) {
            if (Double.isNaN(a.get(i).doubleValue()) || (Double.isNaN(b.get(i).doubleValue()))) {
                d.add(Double.NaN);
            } else {
                d.add(a.get(i) - b.get(i));
            }
        }
        return d;
    }

    double _avg(List<Double> arr, double num) {
        int n = 0;
        double sum = 0.0;
        for (int i = 0; i < num; i++) {
            if (!Double.isNaN(arr.get(i).doubleValue())) {
                sum += arr.get(i);
                n++;
            }
        }
        return sum / n;
    }

    void _set(List<Double> arr, int start, int end, double value) {
        int e = _cmp_min(arr.size(), end);
        for (int i = start; i < e; i++) {
            arr.set(i, value);
        }
    }

    int _skip(List<Double> arr, int period) {
        int j = 0;
        for (int k = 0; j < arr.size(); j++) {
            if (!Double.isNaN(arr.get(j).doubleValue())) {
                k++;
            }
            if (k == period) {
                break;
            }
        }
        return j;
    }

    double _cmp(List<Double> arr, int start, int end, Methods<Double> methods) {
        double v = arr.get(start);
        for (int i = start; i < end; i++) {
            v = methods.methodsChose(arr.get(i).doubleValue(), v);
        }
        return v;
    }
}

interface Methods<T> {
    public T methodsChose(T a, T b);
}
