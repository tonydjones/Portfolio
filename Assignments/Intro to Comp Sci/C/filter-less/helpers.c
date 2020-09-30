#include "helpers.h"
#include <math.h> //for round
#include <stdlib.h> // for free
#include <stdio.h> //for ints

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

// Convert image to sepia
void sepia(int height, int width, RGBTRIPLE image[height][width])
{
    //make variable to keep RBG values
    uint8_t red;
    uint8_t blue;
    uint8_t green;
    //iterate through rows of the image array
    for (int i = 0; i < height; i++)
    {
        //iterate through pixels of row
        for (int j = 0; j < width; j++)
        {
            //save current RGB values of pixels to value keepers made earlier
            red = image[i][j].rgbtRed;
            green = image[i][j].rgbtGreen;
            blue = image[i][j].rgbtBlue;
            //check if calculated red value will be oversaturated
            if (round(.393 * red + .769 * green + .189 * blue) > 255)
            {
                //set to 255 automatically to avoid oversaturation
                image[i][j].rgbtRed = 255;
            }
            //otherwise set pixel value to calculated value
            else
            {
                image[i][j].rgbtRed = round(.393 * red + .769 * green + .189 * blue);
            }
            //check if calculated green value will be oversaturated
            if (round(.349 * red + .686 * green + .168 * blue) > 255)
            {
                //set to 255 automatically to avoid oversaturation
                image[i][j].rgbtGreen = 255;
            }
            //otherwise set pixel value to calculated value
            else
            {
                image[i][j].rgbtGreen = round(.349 * red + .686 * green + .168 * blue);
            }
            //check if calculated blue value will be oversaturated
            if (round(.272 * red + .534 * green + .131 * blue) > 255)
            {
                //set to 255 automatically to avoid oversaturation
                image[i][j].rgbtBlue = 255;
            }
            //otherwise set blue value to calculated value
            else
            {
                image[i][j].rgbtBlue = round(.272 * red + .534 * green + .131 * blue);
            }
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