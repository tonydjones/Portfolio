#include "helpers.h"
#include <math.h>
#include <stdlib.h>

// Convert image to grayscale
void grayscale(int height, int width, RGBTRIPLE image[height][width])
{
    //iterate through rows of the image array
    for (int i = 0; i < height; i++)
    {
        //iterate through pixels of row
        for (int j = 0; j < width; j++)
        {
            //set Rbyte to the average of the RBGTRIPLE bytes
            image[i][j].rgbtRed = round((float)(image[i][j].rgbtRed + image[i][j].rgbtBlue + image[i][j].rgbtGreen) / 3);
            //set blue and green bytes to be equal to redbyte
            image[i][j].rgbtBlue = image[i][j].rgbtRed;
            image[i][j].rgbtGreen = image[i][j].rgbtRed;
        }
    }
    return;
}

// Reflect image horizontally
void reflect(int height, int width, RGBTRIPLE image[height][width])
{
    //iterate through rows of the image array
    for (int i = 0; i < height; i++)
    {
        //iterate through pixels of row
        for (int j = 1; j <= width-j; j++)
        {
            //place current RGBTRIPLE in tmp
            RGBTRIPLE tmp = *(&image[i][j-1]);
            //place opposite RGBTRIPLE in current position
            *(&image[i][j-1]) = *(&image[i][width-j]);
            //place tmp RGBTRIPLE in opposite position (effectively switching current and opposite triples)
            *(&image[i][width-j]) = tmp;
        }
    }
    return;
}

// Blur image
void blur(int height, int width, RGBTRIPLE image[height][width])
{
    //temporary 2D array to store new RBGTRIPLES
    RGBTRIPLE temp[height][width];
    //iterate through rows of the image array
    for (int i = 0; i < height; i++)
    {
        //iterate through pixels of row
        for (int j = 0; j < width; j++)
        {
            //create temporary counters sum and pixels for use determining the average later
            int redsum = 0;
            int greensum = 0;
            int bluesum = 0;
            int pixels = 0;
            //cycle through the rows around the current pixel
            for (int k = i-1; k < i + 2; k++)
            {
                //cycle through the columns around the current pixel
                for (int l = j-1; l < j + 2; l++)
                //check to make sure the pixel location is within the image
                if (k >= 0 && k < height && l >= 0 && l < width)
                {
                    //increment pixel counter
                    pixels++;
                    //add the RGB values to their respective sums
                    redsum += image[k][l].rgbtRed;
                    greensum += image[k][l].rgbtGreen;
                    bluesum += image[k][l].rgbtBlue;
                }
            }
            //set RGB values of current pixel to the average of the RGB values
            temp[i][j].rgbtRed = round((float)redsum / pixels);
            temp[i][j].rgbtGreen = round((float)greensum / pixels);
            temp[i][j].rgbtBlue = round((float)bluesum / pixels);
        }
    }
    //iterate through rows of the image array
    for (int i = 0; i < height; i++)
    {
        //iterate through pixels of row
        for (int j = 0; j < width; j++)
        {
            //change image RBG values to tmp RBG values
            image[i][j] = *&temp[i][j];
        }
    }
    return;
}

// Detect edges
void edges(int height, int width, RGBTRIPLE image[height][width])
{
    //temporary 2D array to store new RBGTRIPLES
    RGBTRIPLE temp[height][width];
    //iterate through rows of the image array
    for (int i = 0; i < height; i++)
    {
        //iterate through pixels of row
        for (int j = 0; j < width; j++)
        {
            //create temporary counters for Gx and Gy
            int gxred = 0;
            int gyred = 0;
            int gxblue = 0;
            int gyblue = 0;
            int gxgreen = 0;
            int gygreen = 0;
            //cycle through the rows around the current pixel
            for (int k = i-1; k < i + 2; k++)
            {
                //cycle through the columns around the current pixel
                for (int l = j-1; l < j + 2; l++)
                //check to make sure the pixel location is within the image
                if (k >= 0 && k < height && l >= 0 && l < width)
                {
                    //calculate gx and gy operators for RGB
                    gxred += image[k][l].rgbtRed * (l - j) * (2 - abs(k - i));
                    gyred += image[k][l].rgbtRed * (k - i) * (2 - abs(l - j));
                    gxblue += image[k][l].rgbtBlue * (l - j) * (2 - abs(k - i));
                    gyblue += image[k][l].rgbtBlue * (k - i) * (2 - abs(l - j));
                    gxgreen += image[k][l].rgbtGreen * (l - j) * (2 - abs(k - i));
                    gygreen += image[k][l].rgbtGreen * (k - i) * (2 - abs(l - j));
                }
            }
            //set RGB values of current pixel (in temp array) to the calculated Sobel operator sqrt(gx^2+gy^2)
            if (round(sqrt((float)gxred * gxred + (float)gyred * gyred)) > 255)
            {
                temp[i][j].rgbtRed = 255;
            }
            else
            {
                temp[i][j].rgbtRed = round(sqrt((float)gxred * gxred + (float)gyred * gyred));
            }
            if (round(sqrt((float)gxblue * gxblue + (float)gyblue * gyblue)) > 255)
            {
                temp[i][j].rgbtBlue = 255;
            }
            else
            {
                temp[i][j].rgbtBlue = round(sqrt((float)gxblue * gxblue + (float)gyblue * gyblue));
            }
            if (round(sqrt((float)gxgreen * gxgreen + (float)gygreen * gygreen)) > 255)
            {
                temp[i][j].rgbtGreen = 255;
            }
            else
            {
                temp[i][j].rgbtGreen = round(sqrt((float)gxgreen * gxgreen + (float)gygreen * gygreen));
            }
        }
    }
    //iterate through rows of the image array
    for (int i = 0; i < height; i++)
    {
        //iterate through pixels of row
        for (int j = 0; j < width; j++)
        {
            //change image RBG values to tmp RBG values
            image[i][j] = *&temp[i][j];
        }
    }
    return;
    return;
}
