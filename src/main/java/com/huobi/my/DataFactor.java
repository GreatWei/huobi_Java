package com.huobi.my;

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

        array<vector<double>, 3> MACD(Records &records, size_t fastEMA = 12, size_t slowEMA = 26, size_t signalEMA = 9) {
            vector<double> ticks = records.Close();
            vector<double> dif = _diff(_ema(ticks, fastEMA), _ema(ticks, slowEMA));
            vector<double> signal = _ema(dif, signalEMA);
            vector<double> histogram = _diff(dif, signal);
            return {{ dif, signal, histogram }};
        }

        array<vector<double>, 3> KDJ(Records &records, size_t n = 9, size_t k = 3, size_t d = 3) {
            size_t length = records.size();
            vector<double> RSV(length, 0);
            _set(RSV, 0, n - 1, NAN);
            vector<double> K(length, 0);
            vector<double> D(length, 0);
            vector<double> J(length, 0);

            vector<double> hs = records.High();
            vector<double> ls = records.Low();

            for (size_t i = 0; i < length; i++) {
                if (i >= size_t(n - 1)) {
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
            for (size_t i = 0; i < n - 1; i++) {
                K[i] = D[i] = J[i] = NAN;
            }
            return{{ K, D, J }};
        }

        vector<double> RSI(Records &records, size_t period = 14) {
            size_t i = 0;
            size_t n = period;
            vector<double> rsi(records.size(), 0);
            _set(rsi, 0, rsi.size(), NAN);
            if (records.size() < n) {
                return rsi;
            }
            vector<double> ticks = records.Close();
            vector<double> deltas = _move_diff(ticks);
            vector<double> seed(deltas.begin(), deltas.begin() + n);
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

        vector<double> ATR(Records &records, size_t period = 14) {
            vector<double> ret;
            if (records.size() == 0) {
                return ret;
            }
            vector<double> R(records.size(), 0);
            double sum = 0.0;
            double n = 0.0;
            for (size_t i = 0; i < records.size(); i++) {
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

        vector<double> OBV(Records &records) {
            vector<double> R;
            if (records.size() == 0) {
                return R;
            }
            for (size_t i = 0; i < records.size(); i++) {
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

        vector<double> MA(Records &records, size_t period = 9) {
            return _sma(records.Close(), period);
        }

        vector<double> EMA(Records &records, size_t period = 9) {
            return _ema(records.Close(), period);
        }

        array<vector<double>, 3> BOLL(Records &records, size_t period = 20, double multiplier = 2) {
            vector<double> S = records.Close();
            size_t j = 0;
            for (j = period - 1; j < S.size() && isnan(S[j]); j++);
            vector<double> UP(S.size(), 0);
            vector<double> MB(S.size(), 0);
            vector<double> DN(S.size(), 0);
            _set(UP, 0, j, NAN);
            _set(MB, 0, j, NAN);
            _set(DN, 0, j, NAN);
            double sum = 0;
            for (size_t i = j; i < S.size(); i++) {
                if (i == j) {
                    for (size_t k = 0; k < period; k++) {
                        sum += S[k];
                    }
                } else {
                    sum = sum + S[i] - S[i - period];
                }
                double ma = sum / period;
                double d = 0.0;
                for (size_t k = i + 1 - period; k <= i; k++) {
                    d += (S[k] - ma) * (S[k] - ma);
                }
                double stdev = sqrt(d / period);
                double up = ma + (multiplier * stdev);
                double dn = ma - (multiplier * stdev);
                UP[i] = up;
                MB[i] = ma;
                DN[i] = dn;
            }
            return {{ UP, MB, DN }};
        }

        array<vector<double>, 3> Alligator(Records &records, size_t jawLength = 13, size_t teethLength = 8, size_t lipsLength = 5) {
            vector<double> ticks;
            for (size_t i = 0; i < records.size(); i++) {
                ticks.push_back((records[i].High + records[i].Low) / 2);
            }
            vector<double> jaw = _smma(ticks, jawLength);
            jaw.insert(jaw.begin(), 8, NAN);
            vector<double> teeth = _smma(ticks, teethLength);
            teeth.insert(teeth.begin(), 5, NAN);
            vector<double> lips = _smma(ticks, lipsLength);
            lips.insert(lips.begin(), 3, NAN);
            return{{ jaw, teeth, lips }};
        }

        vector<double> CMF(Records &records, size_t periods = 20) {
            vector<double> ret;
            double sumD = 0.0;
            double sumV = 0.0;
            vector<double> arrD;
            vector<double> arrV;
            for (size_t i = 0; i < records.size(); i++) {
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

        double Highest(vector<double> records, size_t n) {
            return _filt(records, n, NAN, _cmp_max);
        }

        double Lowest(vector<double> records, size_t n) {
            return _filt(records, n, NAN, _cmp_min);
        }

        double _filt(vector<double> records, double n, double iv, double(*pfun) (double a, double b)) {
            if (records.size() < 2) {
                return NAN;
            }
            double v = iv;
            double pos = n != 0 ? records.size() - _cmp_min(records.size() - 1, n) - 1 : 0;
            for (size_t i = records.size() - 2; i >= pos; i--) {
                v = pfun(v, records[i]);
            }
            return v;
        }

        vector<double> _smma(vector<double> S, size_t period) {
            size_t length = S.size();
            vector<double> R(length, 0);
            size_t j = _skip(S, period);
            _set(R, 0, j, NAN);
            if (j < length) {
                R[j] = _avg(S, j + 1);
                for (size_t i = j + 1; i < length; i++) {
                    R[i] = (R[i - 1] * (period - 1) + S[i]) / period;
                }
            }
            return R;
        }

        vector<double> _move_diff(vector<double> a) {
            vector<double> d;
            for (size_t i = 1; i < a.size(); i++) {
                d.push_back(a[i] - a[i - 1]);
            }
            return d;
        }

        vector<double> _ema(vector<double> S, size_t period) {
            size_t length = S.size();
            vector<double> R(length, 0);
            double multiplier = 2.0 / (period + 1);
            size_t j = _skip(S, period);
            _set(R, 0, j, NAN);
            if (j < length) {
                R[j] = _avg(S, j + 1);
                for (size_t i = j + 1; i < length; i++) {
                    R[i] = (S[i] - R[i - 1]) * multiplier + R[i - 1];
                }
            }
            return R;
        }

        vector<double> _sma(vector<double> S, size_t period) {
            vector<double> R(S.size(), 0);
            size_t j = _skip(S, period);
            _set(R, 0, j, NAN);
            if (j < S.size()) {
                double sum = 0;
                for (size_t i = j; i < S.size(); i++) {
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

        double _sum(vector<double> arr, size_t num) {
            double sum = 0.0;
            for (size_t i = 0; i < num; i++) {
                if (!isnan(arr[i])) {
                    sum += arr[i];
                }
            }
            return sum;
        }

        vector<double> _diff(vector<double> a, vector<double> b) {
            vector<double> d;
            for (size_t i = 0; i < b.size(); i++) {
                if (isnan(a[i]) || isnan(b[i])) {
                    d.push_back(NAN);
                } else {
                    d.push_back(a[i] - b[i]);
                }
            }
            return d;
        }

        double _avg(vector<double> arr, double num) {
            size_t n = 0;
            double sum = 0.0;
            for (size_t i = 0; i < num; i++) {
                if (!isnan(arr[i])) {
                    sum += arr[i];
                    n++;
                }
            }
            return sum / n;
        }

        void _set(vector<double> &arr, size_t start, size_t end, double value) {
            size_t e = _cmp_min(arr.size(), end);
            for (size_t i = start; i < e; i++) {
                arr[i] = value;
            }
        }

        size_t _skip(vector<double> arr, size_t period) {
            size_t j = 0;
            for (size_t k = 0; j < arr.size(); j++) {
                if (!isnan(arr[j])) {
                    k++;
                }
                if (k == period) {
                    break;
                }
            }
            return j;
        }

        double _cmp(vector<double> arr, size_t start, size_t end, double(*pfun) (double a, double b)) {
            double v = arr[start];
            for (size_t i = start; i < end; i++) {
                v = pfun(arr[i], v);
            }
            return v;
        }
    };
}
