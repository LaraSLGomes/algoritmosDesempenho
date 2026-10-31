import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

class MergeSort {
    public static void sortSerial(int[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            sortSerial(arr, left, mid);
            sortSerial(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }

    private static void merge(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        while (i <= mid && j <= right) {
            temp[k++] = (arr[i] <= arr[j]) ? arr[i++] : arr[j++];
        }
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        System.arraycopy(temp, 0, arr, left, temp.length);
    }

    static class MergeSortTask extends RecursiveAction {
        int[] arr; int left, right;
        MergeSortTask(int[] arr, int left, int right) { this.arr = arr; this.left = left; this.right = right; }
        
        @Override protected void compute() {
            if (left < right) {
                if (right - left < 10000) { 
                    sortSerial(arr, left, right);
                } else {
                    int mid = (left + right) / 2;
                    invokeAll(new MergeSortTask(arr, left, mid), new MergeSortTask(arr, mid + 1, right));
                    merge(arr, left, mid, right);
                }
            }
        }
    }
}

class QuickSort {
    public static void sortSerial(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            sortSerial(arr, low, pi - 1);
            sortSerial(arr, pi + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                int temp = arr[i]; arr[i] = arr[j]; arr[j] = temp;
            }
        }
        int temp = arr[i + 1]; arr[i + 1] = arr[high]; arr[high] = temp;
        return i + 1;
    }

    static class QuickSortTask extends RecursiveAction {
        int[] arr; int low, high;
        QuickSortTask(int[] arr, int low, int high) { this.arr = arr; this.low = low; this.high = high; }
        
        @Override protected void compute() {
            if (low < high) {
                if (high - low < 10000) { 
                    sortSerial(arr, low, high);
                } else {
                    int pi = partition(arr, low, high);
                    invokeAll(new QuickSortTask(arr, low, pi - 1), new QuickSortTask(arr, pi + 1, high));
                }
            }
        }
    }
}

class BubbleSort {
    public static void sortSerial(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j]; arr[j] = arr[j + 1]; arr[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    public static void sortParallelOddEven(int[] arr, ForkJoinPool pool) {
        int n = arr.length;
        boolean isSorted = false;
        
        while (!isSorted) {
            isSorted = true;
            final boolean[] swapped = {false};
            
            pool.invoke(new RecursiveAction() {
                @Override protected void compute() {
                    for (int i = 1; i <= n - 2; i += 2) {
                        if (arr[i] > arr[i + 1]) {
                            int temp = arr[i]; arr[i] = arr[i + 1]; arr[i + 1] = temp;
                            swapped[0] = true;
                        }
                    }
                }
            });
            pool.invoke(new RecursiveAction() {
                @Override protected void compute() {
                    for (int i = 0; i <= n - 2; i += 2) {
                        if (arr[i] > arr[i + 1]) {
                            int temp = arr[i]; arr[i] = arr[i + 1]; arr[i + 1] = temp;
                            swapped[0] = true;
                        }
                    }
                }
            });
            if (swapped[0]) isSorted = false;
        }
    }
}

class CountingSort {
    public static void sortSerial(int[] arr) {
        if (arr.length == 0) return;
        int max = Arrays.stream(arr).max().getAsInt();
        int[] count = new int[max + 1];
        int[] output = new int[arr.length];
        
        for (int j : arr) count[j]++;
        for (int i = 1; i <= max; i++) count[i] += count[i - 1];
        for (int i = arr.length - 1; i >= 0; i--) {
            output[count[arr[i]] - 1] = arr[i];
            count[arr[i]]--;
        }
        System.arraycopy(output, 0, arr, 0, arr.length);
    }

    static class CountingSortTask extends RecursiveAction {
        int[] arr;
        CountingSortTask(int[] arr) { this.arr = arr; }
        
        @Override protected void compute() {
            int[] sorted = Arrays.stream(arr).parallel().sorted().toArray();
            System.arraycopy(sorted, 0, arr, 0, arr.length);
        }
    }
}