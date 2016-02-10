package cpslab.inhwan.cpslogger_v02;

/**
 * Created by Inhwan on 2015-10-01.
 */
public class Func {

    public static double sum(double[] array) {
        double sum = 0.0;

        for (int i = 0; i < array.length; i++)
            sum += array[i];

        return sum;
    }

    public static double average(double[] array) {
        double sum = 0.0;

        for (int i = 0; i < array.length; i++)
            sum += array[i];

        return sum / array.length;
    }

    public static double std(double[] array) {
        double sum1 = 0.0, sum2 = 0.0, av1 = 0.0, av2 = 0.0;

        for (int i = 0; i < array.length; i++){
            sum1 += array[i];
        }
        av1 = sum1 / array.length;

        for (int i = 0; i < array.length; i++){
            sum2 += Math.pow((array[i]-av1), 2);
        }
        av2 = sum2 / array.length;

        return Math.sqrt(av2);
    }

    public static double max(double[] array) {
        double max = array[0];

        for (int i = 1; i < array.length; i++)
            if (array[i] > max) max = array[i];

        return max;
    }

    public static int max(int[] array) {
        int max = array[0];

        for (int i = 1; i < array.length; i++)
            if (array[i] > max) max = array[i];

        return max;
    }

    public static int maxNo(double[] array) {
        double max = array[0];
        int No=0;

        for (int i = 1; i < array.length; i++)
            if (array[i] > max) {
                max = array[i];
                No=i;
            }

        return No;
    }

    public static int maxNo(int[] array) {
        int max = array[0];
        int No=0;

        for (int i = 1; i < array.length; i++)
            if (array[i] > max) {
                max = array[i];
                No=i;
            }

        return No;
    }

    public static double[] maxSet(double[] array, int No) {
        double[] max = new double[No];
        int[] a = new int[No];
        double[] tempArr = new double[array.length];
        double temp = min(array);
        for(int i=0; i<array.length; i++) {
            tempArr[i] = array[i];
        }
        for (int i=0; i<No; i++) {
            max[i] = max(tempArr);
            a[i] = maxNo(tempArr);
            tempArr[a[i]] = temp;
        }
        return max;
    }

    public static int[] maxSetNo(double[] array, int No) {
        double[] max = new double[No];
        int[] a = new int[No];
        double[] tempArr = new double[array.length];
        double temp = min(array);
        for(int i=0; i<array.length; i++) {
            tempArr[i] = array[i];
        }
        for (int i=0; i<No; i++) {
            max[i] = max(tempArr);
            a[i] = maxNo(tempArr);
            tempArr[a[i]] = temp;
        }
        return a;
    }

    public static double min(double[] array) {
        double min = array[0];

        for (int i = 1; i < array.length; i++)
            if (array[i] < min) min = array[i];

        return min;
    }

    public static int minNo(double[] array) {
        double min = array[0];
        int No=0;

        for (int i = 1; i < array.length; i++)
            if (array[i] < min) {
                min = array[i];
                No=i;
            }

        return No;
    }

    public static double[] minSet(double[] array, int No) {
        double[] min = new double[No];
        int[] a = new int[No];
        double[] tempArr = new double[array.length];
        double temp = max(array);
        for(int i=0; i<array.length; i++) {
            tempArr[i] = array[i];
        }
        for (int i=0; i<No; i++) {
            min[i] = min(tempArr);
            a[i] = minNo(tempArr);
            tempArr[a[i]] = temp;
        }
        return min;
    }

    public static int[] minSetNo(double[] array, int No) {
        double[] min = new double[No];
        int[] a = new int[No];
        double[] tempArr = new double[array.length];
        double temp = max(array);
        for(int i=0; i<array.length; i++) {
            tempArr[i] = array[i];
        }
        for (int i=0; i<No; i++) {
            min[i] = min(tempArr);
            a[i] = minNo(tempArr);
            tempArr[a[i]] = temp;
        }
        return a;
    }

    public static double normalize(double dat, double mean, double std) {
        double n=0;
        if(std==0) n=0;
        else	n = ((dat-mean)/std);
        return n;
    }

    public static double[] project(double[] mat1, double[][] mat2) {
        int a, b;
        a = mat1.length;
        b = mat2[0].length;
        double[] mat = new double[b];
        for(int i = 0; i < b; i++) {
            for(int j = 0; j < a; j++) {
                mat[i] += mat1[j]*mat2[j][i];
            }
        }
        return mat;
    }

    public static double[] project(float[] mat1, double[][] mat2) {
        int a, b;
        a = mat1.length;
        b = mat2[0].length;
        double[] mat = new double[b];
        for(int i = 0; i < b; i++) {
            for(int j = 0; j < a; j++) {
                mat[i] += mat1[j]*mat2[j][i];
            }
        }
        return mat;
    }

    public static double[] hist(double[] array, double min, double max, double d) {
        double a = (max*10-min*10);
        double b = a/(d*10);
        int c = (int) b;
        int num = c+1;
        double[] bin = new double[num];

        for (int i = 0; i < array.length; i++) {
            double sw = array[i];
            if (sw <= min+d/2) bin[0]++;
            else if (sw > (min+d*num)-d/2) bin[num-1]++;
            for (int j = 1; j < num-1; j++) {
                if((sw > (min+d*j)-d/2) && (sw <= (min+d*j)+d/2)) bin[j]++;
            }
        }
        return bin;
    }

    public static double sgn(double a) {
        double sgn=0;
        if (a==0) sgn=0;
        else if (a>0) sgn=1;
        else sgn=-1;
        return sgn;
    }

    public static double mcr(double[] array) {
        double mcr=0;
        for (int i=0; i<array.length-1; i++)	mcr+=(Math.abs(sgn(array[i]-average(array))-sgn(array[i+1]-average(array))))/2;
        return mcr;
    }

    public static double ent(double[] array) {
        double ent=0;
        double sum=sum(array);
        for (int i=0; i<array.length; i++) {
            ent-=(array[i]/sum)*(Math.log((array[i]/sum))/Math.log(2));
        }
        return ent;
    }

    public static double[][] trans(double[][] mat) {
        double[][] tmat = new double[mat[0].length][mat.length];
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                tmat[j][i] = mat[i][j];
            }
        }
        return tmat;
    }

    public static double[][] matX(double[][] mat1, double[][]mat2) {
        double[][] mat = new double[mat1.length][mat2[0].length];
        if (mat1[0].length != mat2.length) {
            throw new Error("matrix dimmension is not matching");
        }
        else if (mat1[0].length == mat2.length) {
            for (int i = 0; i < mat1.length; i++) {
                for (int j = 0; j < mat2[0].length; j++) {
                    for (int k = 0; k < mat2.length; k++) {
                        mat[i][j] += mat1[i][k]*mat2[k][j];
                    }
                }
            }
        }
        return mat;
    }

    public static int[] cumsum(int[] array) {
        int[] cumsum = new int[array.length];
        for(int i = 0; i < array.length; i++) {
            cumsum[i] = array[i];
        }
        for(int i = 1; i < array.length; i++) {
            cumsum[i] += cumsum[i-1];
        }
        return cumsum;
    }

    public static double norm2(double[] array1, double[] array2) {
        double norm = 0;
        double accsum = 0;

        for (int i = 0; i < array1.length; i++) {
            accsum += Math.pow(array1[i]-array2[i], 2);
        }
        norm = Math.sqrt(accsum);

        return norm;
    }

}
