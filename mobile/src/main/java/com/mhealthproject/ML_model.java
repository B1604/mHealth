package com.mhealthproject;

/**
 * Created by B on 3/4/2016.
 */
public class ML_model {
    public static void is_stressed(){
        float accel = MainActivity.accelerometer;
        float hr = MainActivity.hr;
        float t_major = MainActivity.t_major;
        float t_minor = MainActivity.t_minor;
        float t_time = MainActivity.t_time;
        float t_x = MainActivity.t_x;
        float t_y = MainActivity.t_y;
        float t_size = MainActivity.t_size;
        float t_pres = MainActivity.t_pressure;
        boolean stress;

        if(hr <= 0){
            stress = true;
        }
        else if(t_pres <= 1996){
            if(hr <= 116){
                if(accel <= -7.37893)
                    stress = false;
                else if(hr <= 63){
                    if(accel <= -0.222661){
                        if(t_minor <= 5.623204){
                            if(accel <= -1.070208){
                                if (accel <= -5.291186)
                                    stress = false;
                                else if(hr <=60){
                                    if(hr <= 58){
                                        if(hr <= 54)
                                            stress = false;
                                        else
                                            stress = true;
                                    }
                                    else
                                        stress= false;
                                }else
                                    stress = true;
                            }
                            else if(accel <= -0.995988){
                                if(accel <= -1.053449){
                                    if(hr <= 61)
                                        stress = false;
                                    else
                                        stress = true;
                                }else
                                    stress = false;
                            }
                            else if(accel <= -0.620098){
                                if(hr <= 59)
                                    stress = false;
                                else
                                    stress = true;
                            }
                            else if(accel <= -0.409408){
                                if(accel <= -0.512359)
                                    stress = false;
                                else
                                    stress = true;
                            }
                            else
                                stress = false;
                        }
                        else
                            stress = false;
                    }
                    else if(hr <= 57)
                        stress = false;
                    else
                        stress = true;
                }
                else if(accel <= -1.053449){
                    if(t_x <= 11363.6)
                        stress = true;
                    else if(t_y <= 22879.375)
                        stress = true;
                    else
                        stress = false;
                }
                else if(hr <= 81){
                    if(t_major <= 2262.5){
                        if(hr<= 65){
                            if (accel <= -0.28491){
                                if(accel <= -0.459687){
                                    if(accel <= -1.000776)
                                        stress = false;
                                    else if(t_x <= 8863.068)
                                        stress = true;
                                    else
                                        stress = false;
                                }else
                                    stress = false;
                            }else
                                stress = true;
                        }
                        else if(t_minor <= 70){
                            if(t_pres <= 9.823534){
                                if(accel <= -0.560243)
                                    stress = true;
                                else if(accel <= -0.25618){
                                    if(accel<= -0.438139){
                                        if(accel <= -0.481235)
                                            stress = false;
                                        else
                                            stress = true;
                                    }else
                                        stress = false;
                                }
                                else if(accel <= -0.124498)
                                    stress = true;
                                else if(accel <= 0.888249)
                                    stress = false;
                                else
                                    stress = true;
                            }else
                                stress = true;
                        }else
                            stress = false;
                    }else
                        stress = true;
                }
                else if (t_time <= 1.779815){
                    if(t_x <= 29823.164)
                        stress = false;
                    else
                        stress = true;
                }else
                    stress = false;
            }else
                stress = false;
        }else
            stress = true;

        MainActivity.stress.setter(stress);
    }
}
