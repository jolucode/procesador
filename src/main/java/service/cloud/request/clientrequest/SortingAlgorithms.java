package service.cloud.request.clientrequest;

import java.util.Arrays;
import java.util.Random;

public class SortingAlgorithms {
    // Bubble Sort
    /*public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    // Swap arr[j] and arr[j+1]
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }

    // Insertion Sort
    public static void insertionSort(int[] arr) {
        int n = arr.length;
        for (int i = 1; i < n; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j = j - 1;
            }
            arr[j + 1] = key;
        }
    }

    // Quicksort
    public static void quicksort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quicksort(arr, low, pi - 1);
            quicksort(arr, pi + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        return i + 1;
    }

    // Merge Sort
    public static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }

    private static void merge(int[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        int[] L = new int[n1];
        int[] R = new int[n2];

        System.arraycopy(arr, left, L, 0, n1);
        System.arraycopy(arr, mid + 1, R, 0, n2);

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k++] = L[i++];
            } else {
                arr[k++] = R[j++];
            }
        }
        while (i < n1) {
            arr[k++] = L[i++];
        }
        while (j < n2) {
            arr[k++] = R[j++];
        }
    }

    public static void main(String[] args) {
        int[] arr = generateRandomArray(10000000); // Cambia el tamaño para probar

        int[] arrCopy;

        // Bubble Sort
        arrCopy = Arrays.copyOf(arr, arr.length);
        long startTime = System.currentTimeMillis();
        //bubbleSort(arrCopy);
        long endTime = System.currentTimeMillis();
        System.out.println("Bubble Sort Time: " + (endTime - startTime) + " ms");

        // Insertion Sort
        arrCopy = Arrays.copyOf(arr, arr.length);
        startTime = System.currentTimeMillis();
        //insertionSort(arrCopy);
        endTime = System.currentTimeMillis();
        System.out.println("Insertion Sort Time: " + (endTime - startTime) + " ms");

        // Quicksort
        arrCopy = Arrays.copyOf(arr, arr.length);
        startTime = System.currentTimeMillis();
        quicksort(arrCopy, 0, arrCopy.length - 1);
        endTime = System.currentTimeMillis();
        System.out.println("Quicksort Time: " + (endTime - startTime) + " ms");

        // Merge Sort
        arrCopy = Arrays.copyOf(arr, arr.length);
        startTime = System.currentTimeMillis();
        mergeSort(arrCopy, 0, arrCopy.length - 1);
        endTime = System.currentTimeMillis();
        System.out.println("Merge Sort Time: " + (endTime - startTime) + " ms");
    }

    // Utility method to generate an array of random integers
    private static int[] generateRandomArray(int size) {
        Random rand = new Random();
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = rand.nextInt(1000000); // Números entre 0 y 9999
        }
        return arr;
    }*/

    // Método para generar un arreglo aleatorio de enteros
    private static int[] generateRandomArray(int size) {
        Random random = new Random();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(10000); // Números aleatorios entre 0 y 9999
        }
        return array;
    }

    // Algoritmo Quicksort
    public static void quicksort(int[] array, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(array, low, high);
            quicksort(array, low, pivotIndex - 1);
            quicksort(array, pivotIndex + 1, high);
        }
    }

    private static int partition(int[] array, int low, int high) {
        int pivot = array[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (array[j] <= pivot) {
                i++;
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
        int temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;
        return i + 1;
    }

    // Algoritmo Merge Sort
    public static void mergeSort(int[] array, int left, int right) {
        if (left < right) {
            int middle = (left + right) / 2;
            mergeSort(array, left, middle);
            mergeSort(array, middle + 1, right);
            merge(array, left, middle, right);
        }
    }

    private static void merge(int[] array, int left, int middle, int right) {
        int n1 = middle - left + 1;
        int n2 = right - middle;

        int[] leftArray = new int[n1];
        int[] rightArray = new int[n2];

        System.arraycopy(array, left, leftArray, 0, n1);
        System.arraycopy(array, middle + 1, rightArray, 0, n2);

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (leftArray[i] <= rightArray[j]) {
                array[k++] = leftArray[i++];
            } else {
                array[k++] = rightArray[j++];
            }
        }

        while (i < n1) {
            array[k++] = leftArray[i++];
        }

        while (j < n2) {
            array[k++] = rightArray[j++];
        }
    }

    public static void main(String[] args) {
        int[] sizes = {1, 101, 1000, 10000, 50000}; // Tamaños de lista para las pruebas

        System.out.println("N\tQuicksort\tMerge Sort");

        for (int n : sizes) {
            int[] array = generateRandomArray(n);

            // Medir tiempo de ejecución para Quicksort
            int[] quicksortArray = Arrays.copyOf(array, array.length);
            long startTime = System.nanoTime();
            quicksort(quicksortArray, 0, quicksortArray.length - 1);
            long quicksortTime = System.nanoTime() - startTime;

            // Medir tiempo de ejecución para Merge Sort
            int[] mergeSortArray = Arrays.copyOf(array, array.length);
            startTime = System.nanoTime();
            mergeSort(mergeSortArray, 0, mergeSortArray.length - 1);
            long mergeSortTime = System.nanoTime() - startTime;

            // Convertir los tiempos a segundos y mostrar en formato de tabla
            System.out.printf("%d\t%.6f\t%.6f%n", n, quicksortTime / 1e9, mergeSortTime / 1e9);
        }
    }
}
